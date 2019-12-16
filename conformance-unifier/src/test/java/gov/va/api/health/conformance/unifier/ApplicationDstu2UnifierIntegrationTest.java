package gov.va.api.health.conformance.unifier;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.adobe.testing.s3mock.junit4.S3MockRule;
import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.aws.interfaces.s3.junit.AmazonS3BucketUtilities;
import gov.va.api.health.aws.interfaces.s3.junit.AmazonS3ClientServiceMock;
import gov.va.api.health.conformance.unifier.awss3.AmazonS3BucketConfig;
import gov.va.api.health.conformance.unifier.awss3.AmazonS3ClientWriterService;
import gov.va.api.health.conformance.unifier.exception.DuplicateCapabilityResourceException;
import gov.va.api.health.conformance.unifier.fhir.EndpointTypeEnum;
import gov.va.api.health.conformance.unifier.fhir.ResourceTypeEnum;
import gov.va.api.health.dstu2.api.information.WellKnown;
import gov.va.api.health.dstu2.api.resources.Conformance;
import gov.va.api.health.informational.dstu2.conformance.ConformanceStatementProperties;
import java.net.URI;
import java.nio.file.Paths;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

/**
 * Integration test to test DSTU2 unifiers. The integration test uses mocks to emulate external
 * interface calls to metadata endpoints as well as a mock for the Amazon S3. NOTE: for the Amazon
 * S3 mock to run, a web server must be running in the background and that is why the test
 * application.properties sets property spring.main.web-application-type.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(
  classes = Application.class,
  initializers = ConfigFileApplicationContextInitializer.class
)
public class ApplicationDstu2UnifierIntegrationTest {

  /** Class rule for Mock Amazon S3. */
  @ClassRule
  public static final S3MockRule S3_MOCK_RULE =
      S3MockRule.builder().silent().withSecureConnection(false).build();

  private static final String DSTU2_EXAMPLE_METADATA_ENDPOINT_1 =
      "http://fhir/dstu2/example/1/metadata";

  private static final String DSTU2_EXAMPLE_METADATA_ENDPOINT_2 =
      "http://fhir/dstu2/example/2/metadata";

  private static final String DSTU2_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_1 =
      "http://fhir/dstu2/example/1/.well-known/smart-configuration";

  private static final String DSTU2_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_2 =
      "http://fhir/dstu2/example/2/.well-known/smart-configuration";

  /** Application from Spring context. */
  @Autowired private Application unifierApplication;

  /** Amazon S3 Client Writer Service from Spring context. */
  @Autowired private AmazonS3ClientWriterService amazonS3ClientWriterService;

  /** Rest Template from Spring context. */
  @Autowired private RestTemplate restTemplate;

  /** Amazon S3 Bucket Configuration from Spring context. */
  @Autowired private AmazonS3BucketConfig amazonS3BucketConfig;

  /** Mock Rest Service Server used to mock calls to external endpoints. */
  private MockRestServiceServer mockServer;

  /** Object mapper to translate json to/from objects. */
  private ObjectMapper mapper = new ObjectMapper();

  /** Mock Amazon S3 Client. */
  private AmazonS3 s3Client = S3_MOCK_RULE.createS3Client();

  @Autowired private ConformanceStatementProperties properties;

  /** Off nominal case where duplicate metadata endpoint resources are recognized as duplicates. */
  @Test(expected = DuplicateCapabilityResourceException.class)
  @SneakyThrows
  public void dstu2MetadataUnifierDuplicateTest() {
    // Test arguments to test DSTU2 Metadata Unifier.
    final String[] dstu2MetadataCommandLineArgs =
        new String[] {
          ResourceTypeEnum.DSTU2.type(),
          EndpointTypeEnum.METADATA.type(),
          DSTU2_EXAMPLE_METADATA_ENDPOINT_1,
          DSTU2_EXAMPLE_METADATA_ENDPOINT_2,
          DSTU2_EXAMPLE_METADATA_ENDPOINT_1
        };
    // Load DSTU2 Metadata examples from test resources.
    final Conformance dstu2Example1Metadata =
        mapper.readValue(
            Paths.get("src", "test", "resources", "dstu2_example_metadata_1.json").toFile(),
            Conformance.class);
    final Conformance dstu2Example2Metadata =
        mapper.readValue(
            Paths.get("src", "test", "resources", "dstu2_example_metadata_2.json").toFile(),
            Conformance.class);
    // Configure mock server to respond with appropriate example per metadata endpoint.
    mockServer
        .expect(ExpectedCount.manyTimes(), requestTo(new URI(DSTU2_EXAMPLE_METADATA_ENDPOINT_1)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(dstu2Example1Metadata)));
    mockServer
        .expect(ExpectedCount.once(), requestTo(new URI(DSTU2_EXAMPLE_METADATA_ENDPOINT_2)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(dstu2Example2Metadata)));
    // Run the unifier application using the DSTU2 Metadata command line arguments.
    // The transformer will throw a DuplicateCapabilityResourceException once a duplicate resource
    // is identified.
    unifierApplication.run(new DefaultApplicationArguments(dstu2MetadataCommandLineArgs));
  }

  /** Nominal case where 2 metadata endpoints are unified. */
  @Test
  @SneakyThrows
  public void dstu2MetadataUnifierTest() {
    // Test arguments to test DSTU2 Metadata Unifier.
    final String[] dstu2MetadataCommandLineArgs =
        new String[] {
          ResourceTypeEnum.DSTU2.type(),
          EndpointTypeEnum.METADATA.type(),
          DSTU2_EXAMPLE_METADATA_ENDPOINT_1,
          DSTU2_EXAMPLE_METADATA_ENDPOINT_2
        };
    // Load DSTU2 Metadata examples and expected unified result from test resources.
    final Conformance dstu2ExampleMetadataUnifiedExpected =
        mapper.readValue(
            Paths.get("src", "test", "resources", "dstu2_example_metadata_unified.json").toFile(),
            Conformance.class);
    final Conformance dstu2Example1Metadata =
        mapper.readValue(
            Paths.get("src", "test", "resources", "dstu2_example_metadata_1.json").toFile(),
            Conformance.class);
    final Conformance dstu2Example2Metadata =
        mapper.readValue(
            Paths.get("src", "test", "resources", "dstu2_example_metadata_2.json").toFile(),
            Conformance.class);
    // Configure mock server to respond with appropriate example per metadata endpoint.
    mockServer
        .expect(ExpectedCount.once(), requestTo(new URI(DSTU2_EXAMPLE_METADATA_ENDPOINT_1)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(dstu2Example1Metadata)));
    mockServer
        .expect(ExpectedCount.once(), requestTo(new URI(DSTU2_EXAMPLE_METADATA_ENDPOINT_2)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(dstu2Example2Metadata)));
    // Run the unifier application using the DSTU2 Metadata command line arguments.
    unifierApplication.run(new DefaultApplicationArguments(dstu2MetadataCommandLineArgs));
    // Obtain a unified DSTU2 Metadata object via the mocked Amazon S3.
    final Conformance s3UnifiedMetadata =
        mapper.readValue(
            AmazonS3BucketUtilities.getResultFromS3(s3Client, amazonS3BucketConfig.getName()),
            Conformance.class);
    // Verify the result obtained from the mocked Amazon S3 matches the expected object.
    // NOTE: the publication date is set to current time when bean created so override the expected
    // publication date with the current time value.
    dstu2ExampleMetadataUnifiedExpected.date(properties.getPublicationDate());
    assertEquals(dstu2ExampleMetadataUnifiedExpected, s3UnifiedMetadata);
  }

  /**
   * Nominal case where duplicate WellKnown smart configuration endpoints are unified and filtered.
   */
  @Test
  @SneakyThrows
  public void dstu2WellKnownSmartConfigurationUnifierFilterDuplicateTest() {
    // Test arguments to test DSTU2 WellKnown Smart Configuration Unifier.
    final String[] dstu2SmartCommandLineArgs =
        new String[] {
          ResourceTypeEnum.DSTU2.type(),
          EndpointTypeEnum.SMART_CONFIGURATION.type(),
          DSTU2_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_1,
          DSTU2_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_2,
          DSTU2_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_1,
          DSTU2_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_2
        };
    // Load DSTU2 WellKnown Smart Configuration examples and expected unified result from test
    // resources.
    final WellKnown dstu2ExampleSmartUnifiedExpected =
        mapper.readValue(
            Paths.get("src", "test", "resources", "dstu2_example_smart_configuration_unified.json")
                .toFile(),
            WellKnown.class);
    final WellKnown dstu2Example1Smart =
        mapper.readValue(
            Paths.get("src", "test", "resources", "dstu2_example_smart_configuration_1.json")
                .toFile(),
            WellKnown.class);
    final WellKnown dstu2Example2Smart =
        mapper.readValue(
            Paths.get("src", "test", "resources", "dstu2_example_smart_configuration_2.json")
                .toFile(),
            WellKnown.class);
    // Configure mock server to respond with appropriate example per smart configuration endpoint.
    mockServer
        .expect(
            ExpectedCount.manyTimes(),
            requestTo(new URI(DSTU2_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_1)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(dstu2Example1Smart)));
    mockServer
        .expect(
            ExpectedCount.manyTimes(),
            requestTo(new URI(DSTU2_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_2)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(dstu2Example2Smart)));
    // Run the unifier application using the DSTU2 WellKnown Smart Configuration command line
    // arguments.
    unifierApplication.run(new DefaultApplicationArguments(dstu2SmartCommandLineArgs));
    // Obtain a unified DSTU2 WellKnown Smart Configuration object via the mocked Amazon S3.
    final WellKnown s3UnifiedSmart =
        mapper.readValue(
            AmazonS3BucketUtilities.getResultFromS3(s3Client, amazonS3BucketConfig.getName()),
            WellKnown.class);
    // Verify the result obtained from the mocked Amazon S3 matches the expected object.
    assertEquals(dstu2ExampleSmartUnifiedExpected, s3UnifiedSmart);
  }

  /** Nominal case where 2 WellKnown smart configuration endpoints are unified. */
  @Test
  @SneakyThrows
  public void dstu2WellKnownSmartConfigurationUnifierTest() {
    // Test arguments to test DSTU2 WellKnown Smart Configuration Unifier.
    final String[] dstu2SmartCommandLineArgs =
        new String[] {
          ResourceTypeEnum.DSTU2.type(),
          EndpointTypeEnum.SMART_CONFIGURATION.type(),
          DSTU2_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_1,
          DSTU2_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_2
        };
    // Load DSTU2 WellKnown Smart Configuration examples and expected unified result from test
    // resources.
    final WellKnown dstu2ExampleSmartUnifiedExpected =
        mapper.readValue(
            Paths.get("src", "test", "resources", "dstu2_example_smart_configuration_unified.json")
                .toFile(),
            WellKnown.class);
    final WellKnown dstu2Example1Smart =
        mapper.readValue(
            Paths.get("src", "test", "resources", "dstu2_example_smart_configuration_1.json")
                .toFile(),
            WellKnown.class);
    final WellKnown dstu2Example2Smart =
        mapper.readValue(
            Paths.get("src", "test", "resources", "dstu2_example_smart_configuration_2.json")
                .toFile(),
            WellKnown.class);
    // Configure mock server to respond with appropriate example per smart configuration endpoint.
    mockServer
        .expect(
            ExpectedCount.once(), requestTo(new URI(DSTU2_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_1)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(dstu2Example1Smart)));
    mockServer
        .expect(
            ExpectedCount.once(), requestTo(new URI(DSTU2_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_2)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(dstu2Example2Smart)));
    // Run the unifier application using the DSTU2 WellKnown Smart Configuration command line
    // arguments.
    unifierApplication.run(new DefaultApplicationArguments(dstu2SmartCommandLineArgs));
    // Obtain a unified DSTU2 WellKnown Smart Configuration object via the mocked Amazon S3.
    final WellKnown s3UnifiedSmart =
        mapper.readValue(
            AmazonS3BucketUtilities.getResultFromS3(s3Client, amazonS3BucketConfig.getName()),
            WellKnown.class);
    // Verify the result obtained from the mocked Amazon S3 matches the expected object.
    assertEquals(dstu2ExampleSmartUnifiedExpected, s3UnifiedSmart);
  }

  @Before
  public void init() {
    mockServer = MockRestServiceServer.createServer(restTemplate);
    amazonS3ClientWriterService.setS3ClientService(new AmazonS3ClientServiceMock(s3Client));
  }
}
