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
import gov.va.api.health.conformance.unifier.mock.AmazonS3BucketUtilities;
import gov.va.api.health.conformance.unifier.mock.AmazonS3ClientServiceMock;
import gov.va.api.health.r4.api.information.WellKnown;
import gov.va.api.health.r4.api.resources.Capability;
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
 * Integration test to test R4 unifiers. The integration test uses mocks to emulate external
 * interface calls to metadata endpoints as well as a mock for the Amazon S3. NOTE: for the Amazon
 * S3 mock to run, a web server must be running in the background and that is why the test
 * application.properties sets property spring.main.web-application-type.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(
  classes = Application.class,
  initializers = ConfigFileApplicationContextInitializer.class
)
public class ApplicationR4UnifierIntegrationTest {

  /** Class rule for Mock Amazon S3. */
  @ClassRule
  public static final S3MockRule S3_MOCK_RULE =
      S3MockRule.builder().silent().withSecureConnection(false).build();

  private static final String R4_EXAMPLE_METADATA_ENDPOINT_1 = "http://fhir/r4/example/1/metadata";

  private static final String R4_EXAMPLE_METADATA_ENDPOINT_2 = "http://fhir/r4/example/2/metadata";

  private static final String R4_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_1 =
      "http://fhir/r4/example/1/.well-known/smart-configuration";

  private static final String R4_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_2 =
      "http://fhir/r4/example/2/.well-known/smart-configuration";

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

  @Before
  public void init() {
    mockServer = MockRestServiceServer.createServer(restTemplate);
    amazonS3ClientWriterService.setS3ClientService(new AmazonS3ClientServiceMock(s3Client));
  }

  /** Off nominal case where duplicate metadata endpoint resources are recognized as duplicates. */
  @Test(expected = DuplicateCapabilityResourceException.class)
  @SneakyThrows
  public void r4MetadataUnifierDuplicateTest() {
    // Test arguments to test R4 Metadata Unifier.
    final String[] r4MetadataCommandLineArgs =
        new String[] {
          ResourceTypeEnum.R4.type(),
          EndpointTypeEnum.METADATA.type(),
          R4_EXAMPLE_METADATA_ENDPOINT_1,
          R4_EXAMPLE_METADATA_ENDPOINT_2,
          R4_EXAMPLE_METADATA_ENDPOINT_1
        };
    // Load R4 Metadata examples and expected unified result from test resources.
    final Capability r4ExampleMetadataUnifiedExpected =
        mapper.readValue(
            Paths.get("src", "test", "resources", "r4_example_metadata_unified.json").toFile(),
            Capability.class);
    final Capability r4Example1Metadata =
        mapper.readValue(
            Paths.get("src", "test", "resources", "r4_example_metadata_1.json").toFile(),
            Capability.class);
    final Capability r4Example2Metadata =
        mapper.readValue(
            Paths.get("src", "test", "resources", "r4_example_metadata_2.json").toFile(),
            Capability.class);
    // Configure mock server to respond with appropriate example per metadata endpoint.
    mockServer
        .expect(ExpectedCount.manyTimes(), requestTo(new URI(R4_EXAMPLE_METADATA_ENDPOINT_1)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(r4Example1Metadata)));
    mockServer
        .expect(ExpectedCount.once(), requestTo(new URI(R4_EXAMPLE_METADATA_ENDPOINT_2)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(r4Example2Metadata)));
    // Run the unifier application using the R4 Metadata command line arguments.
    // The transformer will throw a DuplicateCapabilityResourceException once a duplicate resource
    // is identified.
    unifierApplication.run(new DefaultApplicationArguments(r4MetadataCommandLineArgs));
  }

  /** Nominal case where 2 metadata endpoints are unified. */
  @Test
  @SneakyThrows
  public void r4MetadataUnifierTest() {
    // Test arguments to test R4 Metadata Unifier.
    final String[] r4MetadataCommandLineArgs =
        new String[] {
          ResourceTypeEnum.R4.type(),
          EndpointTypeEnum.METADATA.type(),
          R4_EXAMPLE_METADATA_ENDPOINT_1,
          R4_EXAMPLE_METADATA_ENDPOINT_2
        };
    // Load R4 Metadata examples and expected unified result from test resources.
    final Capability r4ExampleMetadataUnifiedExpected =
        mapper.readValue(
            Paths.get("src", "test", "resources", "r4_example_metadata_unified.json").toFile(),
            Capability.class);
    final Capability r4Example1Metadata =
        mapper.readValue(
            Paths.get("src", "test", "resources", "r4_example_metadata_1.json").toFile(),
            Capability.class);
    final Capability r4Example2Metadata =
        mapper.readValue(
            Paths.get("src", "test", "resources", "r4_example_metadata_2.json").toFile(),
            Capability.class);
    // Configure mock server to respond with appropriate example per metadata endpoint.
    mockServer
        .expect(ExpectedCount.once(), requestTo(new URI(R4_EXAMPLE_METADATA_ENDPOINT_1)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(r4Example1Metadata)));
    mockServer
        .expect(ExpectedCount.once(), requestTo(new URI(R4_EXAMPLE_METADATA_ENDPOINT_2)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(r4Example2Metadata)));
    // Run the unifier application using the R4 Metadata command line arguments.
    unifierApplication.run(new DefaultApplicationArguments(r4MetadataCommandLineArgs));
    // Obtain a unified R4 Metadata object via the mocked Amazon S3.
    final Capability s3UnifiedMetadata =
        mapper.readValue(
            AmazonS3BucketUtilities.getResultFromS3(s3Client, amazonS3BucketConfig.getBucket()),
            Capability.class);
    // Verify the result obtained from the mocked Amazon S3 matches the expected object.
    assertEquals(r4ExampleMetadataUnifiedExpected, s3UnifiedMetadata);
  }

  /**
   * Nominal case where duplicate WellKnown smart configuration endpoints are unified and filtered.
   */
  @Test
  @SneakyThrows
  public void r4WellKnownSmartConfigurationUnifierFilterDuplicateTest() {
    // Test arguments to test R4 WellKnown Smart Configuration Unifier.
    final String[] r4SmartCommandLineArgs =
        new String[] {
          ResourceTypeEnum.R4.type(),
          EndpointTypeEnum.SMART_CONFIGURATION.type(),
          R4_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_1,
          R4_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_2,
          R4_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_1,
          R4_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_2
        };
    // Load R4 WellKnown Smart Configuration examples and expected unified result from test
    // resources.
    final WellKnown r4ExampleSmartUnifiedExpected =
        mapper.readValue(
            Paths.get("src", "test", "resources", "r4_example_smart_configuration_unified.json")
                .toFile(),
            WellKnown.class);
    final WellKnown r4Example1Smart =
        mapper.readValue(
            Paths.get("src", "test", "resources", "r4_example_smart_configuration_1.json").toFile(),
            WellKnown.class);
    final WellKnown r4Example2Smart =
        mapper.readValue(
            Paths.get("src", "test", "resources", "r4_example_smart_configuration_2.json").toFile(),
            WellKnown.class);
    // Configure mock server to respond with appropriate example per smart configuration endpoint.
    mockServer
        .expect(
            ExpectedCount.manyTimes(),
            requestTo(new URI(R4_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_1)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(r4Example1Smart)));
    mockServer
        .expect(
            ExpectedCount.manyTimes(),
            requestTo(new URI(R4_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_2)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(r4Example2Smart)));
    // Run the unifier application using the R4 WellKnown Smart Configuration command line
    // arguments.
    unifierApplication.run(new DefaultApplicationArguments(r4SmartCommandLineArgs));
    // Obtain a unified R4 WellKnown Smart Configuration object via the mocked Amazon S3.
    final WellKnown s3UnifiedSmart =
        mapper.readValue(
            AmazonS3BucketUtilities.getResultFromS3(s3Client, amazonS3BucketConfig.getBucket()),
            WellKnown.class);
    // Verify the result obtained from the mocked Amazon S3 matches the expected object.
    assertEquals(r4ExampleSmartUnifiedExpected, s3UnifiedSmart);
  }

  /** Nominal case where 2 WellKnown smart configuration endpoints are unified. */
  @Test
  @SneakyThrows
  public void r4WellKnownSmartConfigurationUnifierTest() {
    // Test arguments to test R4 WellKnown Smart Configuration Unifier.
    final String[] r4SmartCommandLineArgs =
        new String[] {
          ResourceTypeEnum.R4.type(),
          EndpointTypeEnum.SMART_CONFIGURATION.type(),
          R4_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_1,
          R4_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_2
        };
    // Load R4 WellKnown Smart Configuration examples and expected unified result from test
    // resources.
    final WellKnown r4ExampleSmartUnifiedExpected =
        mapper.readValue(
            Paths.get("src", "test", "resources", "r4_example_smart_configuration_unified.json")
                .toFile(),
            WellKnown.class);
    final WellKnown r4Example1Smart =
        mapper.readValue(
            Paths.get("src", "test", "resources", "r4_example_smart_configuration_1.json").toFile(),
            WellKnown.class);
    final WellKnown r4Example2Smart =
        mapper.readValue(
            Paths.get("src", "test", "resources", "r4_example_smart_configuration_2.json").toFile(),
            WellKnown.class);
    // Configure mock server to respond with appropriate example per smart configuration endpoint.
    mockServer
        .expect(ExpectedCount.once(), requestTo(new URI(R4_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_1)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(r4Example1Smart)));
    mockServer
        .expect(ExpectedCount.once(), requestTo(new URI(R4_EXAMPLE_SMART_CONFIGURATION_ENDPOINT_2)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(r4Example2Smart)));
    // Run the unifier application using the R4 WellKnown Smart Configuration command line
    // arguments.
    unifierApplication.run(new DefaultApplicationArguments(r4SmartCommandLineArgs));
    // Obtain a unified R4 WellKnown Smart Configuration object via the mocked Amazon S3.
    final WellKnown s3UnifiedSmart =
        mapper.readValue(
            AmazonS3BucketUtilities.getResultFromS3(s3Client, amazonS3BucketConfig.getBucket()),
            WellKnown.class);
    // Verify the result obtained from the mocked Amazon S3 matches the expected object.
    assertEquals(r4ExampleSmartUnifiedExpected, s3UnifiedSmart);
  }
}
