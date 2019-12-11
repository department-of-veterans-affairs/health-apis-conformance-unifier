package gov.va.api.health.conformance.unifier;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.adobe.testing.s3mock.junit4.S3MockRule;
import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.conformance.unifier.awss3.AmazonS3BucketConfig;
import gov.va.api.health.conformance.unifier.awss3.AmazonS3ClientWriterService;
import gov.va.api.health.conformance.unifier.exception.DuplicateCapabilityResourceException;
import gov.va.api.health.conformance.unifier.fhir.EndpointTypeEnum;
import gov.va.api.health.conformance.unifier.fhir.ResourceTypeEnum;
import gov.va.api.health.informational.stu3.capability.CapabilityStatementProperties;
import gov.va.api.health.stu3.api.information.WellKnown;
import gov.va.api.health.stu3.api.resources.CapabilityStatement;
import gov.va.views.amazon.s3.junit.AmazonS3BucketUtilities;
import gov.va.views.amazon.s3.junit.AmazonS3ClientServiceMock;
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
 * Integration test to test STU3 unifiers. The integration test uses mocks to emulate external
 * interface calls to metadata endpoints as well as a mock for the Amazon S3. NOTE: for the Amazon
 * S3 mock to run, a web server must be running in the background and that is why the test
 * application.properties sets property spring.main.web-application-type.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(
  classes = Application.class,
  initializers = ConfigFileApplicationContextInitializer.class
)
public class ApplicationStu3UnifierIntegrationTest {

  /** Class rule for Mock Amazon S3. */
  @ClassRule
  public static final S3MockRule S3_MOCK_RULE =
      S3MockRule.builder().silent().withSecureConnection(false).build();

  private static final String STU3_EXAMPLE_METADATA_ENDPOINT_1 =
      "http://fhir/stu3/example/1/metadata";

  private static final String STU3_EXAMPLE_METADATA_ENDPOINT_2 =
      "http://fhir/stu3/example/2/metadata";

  private static final String STU3_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_1 =
      "http://fhir/stu3/example/1/.well-known/smart-configuration";

  private static final String STU3_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_2 =
      "http://fhir/stu3/example/2/.well-known/smart-configuration";

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

  @Autowired private CapabilityStatementProperties properties;

  @Before
  public void init() {
    mockServer = MockRestServiceServer.createServer(restTemplate);
    amazonS3ClientWriterService.setS3ClientService(new AmazonS3ClientServiceMock(s3Client));
  }

  /** Off nominal case where duplicate metadata endpoint resources are recognized as duplicates. */
  @Test(expected = DuplicateCapabilityResourceException.class)
  @SneakyThrows
  public void stu3MetadataUnifierDuplicateTest() {
    // Test arguments to test STU3 Metadata Unifier.
    final String[] stu3MetadataCommandLineArgs =
        new String[] {
          ResourceTypeEnum.STU3.type(),
          EndpointTypeEnum.METADATA.type(),
          STU3_EXAMPLE_METADATA_ENDPOINT_1,
          STU3_EXAMPLE_METADATA_ENDPOINT_2,
          STU3_EXAMPLE_METADATA_ENDPOINT_1
        };
    // Load STU3 Metadata examples from test resources.
    final CapabilityStatement stu3Example1Metadata =
        mapper.readValue(
            Paths.get("src", "test", "resources", "stu3_example_metadata_1.json").toFile(),
            CapabilityStatement.class);
    final CapabilityStatement stu3Example2Metadata =
        mapper.readValue(
            Paths.get("src", "test", "resources", "stu3_example_metadata_2.json").toFile(),
            CapabilityStatement.class);
    // Configure mock server to respond with appropriate example per metadata endpoint.
    mockServer
        .expect(ExpectedCount.manyTimes(), requestTo(new URI(STU3_EXAMPLE_METADATA_ENDPOINT_1)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(stu3Example1Metadata)));
    mockServer
        .expect(ExpectedCount.once(), requestTo(new URI(STU3_EXAMPLE_METADATA_ENDPOINT_2)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(stu3Example2Metadata)));
    // Run the unifier application using the STU3 Metadata command line arguments.
    // The transformer will throw a DuplicateCapabilityResourceException once a duplicate resource
    // is identified.
    unifierApplication.run(new DefaultApplicationArguments(stu3MetadataCommandLineArgs));
  }

  /** Nominal case where 2 metadata endpoints are unified. */
  @Test
  @SneakyThrows
  public void stu3MetadataUnifierTest() {
    // Test arguments to test STU3 Metadata Unifier.
    final String[] stu3MetadataCommandLineArgs =
        new String[] {
          ResourceTypeEnum.STU3.type(),
          EndpointTypeEnum.METADATA.type(),
          STU3_EXAMPLE_METADATA_ENDPOINT_1,
          STU3_EXAMPLE_METADATA_ENDPOINT_2
        };
    // Load STU3 Metadata examples and expected unified result from test resources.
    final CapabilityStatement stu3ExampleMetadataUnifiedExpected =
        mapper.readValue(
            Paths.get("src", "test", "resources", "stu3_example_metadata_unified.json").toFile(),
            CapabilityStatement.class);
    final CapabilityStatement stu3Example1Metadata =
        mapper.readValue(
            Paths.get("src", "test", "resources", "stu3_example_metadata_1.json").toFile(),
            CapabilityStatement.class);
    final CapabilityStatement stu3Example2Metadata =
        mapper.readValue(
            Paths.get("src", "test", "resources", "stu3_example_metadata_2.json").toFile(),
            CapabilityStatement.class);
    // Configure mock server to respond with appropriate example per metadata endpoint.
    mockServer
        .expect(ExpectedCount.once(), requestTo(new URI(STU3_EXAMPLE_METADATA_ENDPOINT_1)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(stu3Example1Metadata)));
    mockServer
        .expect(ExpectedCount.once(), requestTo(new URI(STU3_EXAMPLE_METADATA_ENDPOINT_2)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(stu3Example2Metadata)));
    // Run the unifier application using the STU3 Metadata command line arguments.
    unifierApplication.run(new DefaultApplicationArguments(stu3MetadataCommandLineArgs));
    // Obtain a unified STU3 Metadata object via the mocked Amazon S3.
    final CapabilityStatement s3UnifiedMetadata =
        mapper.readValue(
            AmazonS3BucketUtilities.getResultFromS3(s3Client, amazonS3BucketConfig.getBucket()),
            CapabilityStatement.class);
    // Verify the result obtained from the mocked Amazon S3 matches the expected object.
    // NOTE: the publication date is set to current time when bean created so override the expected
    // publication date with the current time value.
    stu3ExampleMetadataUnifiedExpected.date(properties.getPublicationDate());
    assertEquals(stu3ExampleMetadataUnifiedExpected, s3UnifiedMetadata);
  }

  /**
   * Nominal case where duplicate WellKnown smart configuration endpoints are unified and filtered.
   */
  @Test
  @SneakyThrows
  public void stu3WellKnownSmartConfigurationUnifierFilterDuplicateTest() {
    // Test arguments to test STU3 WellKnown Smart Configuration Unifier.
    final String[] stu3SmartCommandLineArgs =
        new String[] {
          ResourceTypeEnum.STU3.type(),
          EndpointTypeEnum.SMART_CONFIGURATION.type(),
          STU3_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_1,
          STU3_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_2,
          STU3_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_1,
          STU3_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_2
        };
    // Load STU3 WellKnown Smart Configuration examples and expected unified result from test
    // resources.
    final WellKnown stu3ExampleSmartUnifiedExpected =
        mapper.readValue(
            Paths.get("src", "test", "resources", "stu3_example_smart_configuration_unified.json")
                .toFile(),
            WellKnown.class);
    final WellKnown stu3Example1Smart =
        mapper.readValue(
            Paths.get("src", "test", "resources", "stu3_example_smart_configuration_1.json")
                .toFile(),
            WellKnown.class);
    final WellKnown stu3Example2Smart =
        mapper.readValue(
            Paths.get("src", "test", "resources", "stu3_example_smart_configuration_2.json")
                .toFile(),
            WellKnown.class);
    // Configure mock server to respond with appropriate example per smart configuration endpoint.
    mockServer
        .expect(
            ExpectedCount.manyTimes(),
            requestTo(new URI(STU3_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_1)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(stu3Example1Smart)));
    mockServer
        .expect(
            ExpectedCount.manyTimes(),
            requestTo(new URI(STU3_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_2)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(stu3Example2Smart)));
    // Run the unifier application using the STU3 WellKnown Smart Configuration command line
    // arguments.
    unifierApplication.run(new DefaultApplicationArguments(stu3SmartCommandLineArgs));
    // Obtain a unified STU3 WellKnown Smart Configuration object via the mocked Amazon S3.
    final WellKnown s3UnifiedSmart =
        mapper.readValue(
            AmazonS3BucketUtilities.getResultFromS3(s3Client, amazonS3BucketConfig.getBucket()),
            WellKnown.class);
    // Verify the result obtained from the mocked Amazon S3 matches the expected object.
    assertEquals(stu3ExampleSmartUnifiedExpected, s3UnifiedSmart);
  }

  /** Nominal case where 2 WellKnown smart configuration endpoints are unified. */
  @Test
  @SneakyThrows
  public void stu3WellKnownSmartConfigurationUnifierTest() {
    // Test arguments to test STU3 WellKnown Smart Configuration Unifier.
    final String[] stu3SmartCommandLineArgs =
        new String[] {
          ResourceTypeEnum.STU3.type(),
          EndpointTypeEnum.SMART_CONFIGURATION.type(),
          STU3_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_1,
          STU3_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_2
        };
    // Load STU3 WellKnown Smart Configuration examples and expected unified result from test
    // resources.
    final WellKnown stu3ExampleSmartUnifiedExpected =
        mapper.readValue(
            Paths.get("src", "test", "resources", "stu3_example_smart_configuration_unified.json")
                .toFile(),
            WellKnown.class);
    final WellKnown stu3Example1Smart =
        mapper.readValue(
            Paths.get("src", "test", "resources", "stu3_example_smart_configuration_1.json")
                .toFile(),
            WellKnown.class);
    final WellKnown stu3Example2Smart =
        mapper.readValue(
            Paths.get("src", "test", "resources", "stu3_example_smart_configuration_2.json")
                .toFile(),
            WellKnown.class);
    // Configure mock server to respond with appropriate example per smart configuration endpoint.
    mockServer
        .expect(
            ExpectedCount.once(), requestTo(new URI(STU3_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_1)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(stu3Example1Smart)));
    mockServer
        .expect(
            ExpectedCount.once(), requestTo(new URI(STU3_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_2)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(stu3Example2Smart)));
    // Run the unifier application using the STU3 WellKnown Smart Configuration command line
    // arguments.
    unifierApplication.run(new DefaultApplicationArguments(stu3SmartCommandLineArgs));
    // Obtain a unified STU3 WellKnown Smart Configuration object via the mocked Amazon S3.
    final WellKnown s3UnifiedSmart =
        mapper.readValue(
            AmazonS3BucketUtilities.getResultFromS3(s3Client, amazonS3BucketConfig.getBucket()),
            WellKnown.class);
    // Verify the result obtained from the mocked Amazon S3 matches the expected object.
    assertEquals(stu3ExampleSmartUnifiedExpected, s3UnifiedSmart);
  }
}
