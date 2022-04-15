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
import gov.va.api.health.conformance.unifier.exception.OpenApiDuplicateKeyConflictException;
import gov.va.api.health.conformance.unifier.exception.OpenApiPathDuplicateException;
import gov.va.api.health.conformance.unifier.fhir.EndpointTypeEnum;
import gov.va.api.health.conformance.unifier.fhir.ResourceTypeEnum;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import java.net.URI;
import java.nio.file.Paths;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

/**
 * Integration test to test OpenAPI unifiers. The integration test uses mocks to emulate external
 * interface calls to metadata endpoints as well as a mock for the Amazon S3. NOTE: for the Amazon
 * S3 mock to run, a web server must be running in the background and that is why the test
 * application.properties sets property spring.main.web-application-type.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(
    classes = Application.class,
    initializers = ConfigDataApplicationContextInitializer.class)
public class ApplicationOpenApiUnifierIntegrationTest {
  /** Class rule for Mock Amazon S3. */
  @ClassRule
  public static final S3MockRule S3_MOCK_RULE =
      S3MockRule.builder().silent().withSecureConnection(false).build();

  private static final String R4_EXAMPLE_OPENAPI_ENDPOINT_1 = "http://fhir/example/1/openapi.json";

  private static final String R4_EXAMPLE_OPENAPI_ENDPOINT_2 = "http://fhir/example/2/openapi.json";

  /** Application from Spring context. */
  @Autowired private Application unifierApplication;

  /** Amazon S3 Client Writer Service from Spring context. */
  @Autowired private AmazonS3ClientWriterService amazonS3ClientWriterService;

  /** Rest Template from Spring context. */
  @Autowired private RestTemplate restTemplate;

  /** Amazon S3 Bucket Configuration from Spring context. */
  @Autowired private AmazonS3BucketConfig amazonS3BucketConfig;

  /** Mock Amazon S3 Client. */
  private AmazonS3 s3Client = S3_MOCK_RULE.createS3Client();

  /** Mock Rest Service Server used to mock calls to external endpoints. */
  private MockRestServiceServer mockServer;

  /** Customized Object mapper for OpenApi json */
  private ObjectMapper mapperOpenapi = Json.mapper();

  @Before
  public void init() {
    mockServer = MockRestServiceServer.createServer(restTemplate);
    amazonS3ClientWriterService.setS3ClientService(new AmazonS3ClientServiceMock(s3Client));
  }

  @Test(expected = OpenApiPathDuplicateException.class)
  @SneakyThrows
  public void openApiDuplicatePathTest() {
    // Test arguments to test R4 Metadata Unifier.
    final String[] r4MetadataCommandLineArgs =
        new String[] {
          ResourceTypeEnum.R4.type(),
          EndpointTypeEnum.OPENAPI.type(),
          R4_EXAMPLE_OPENAPI_ENDPOINT_1,
          R4_EXAMPLE_OPENAPI_ENDPOINT_2
        };
    final OpenAPI r4ExampleOpenapi1 =
        mapperOpenapi.readValue(
            Paths.get("src", "test", "resources", "r4_openapi_example_1.json").toFile(),
            OpenAPI.class);
    final OpenAPI r4ExampleDuplicatePathOpenapi =
        mapperOpenapi.readValue(
            Paths.get("src", "test", "resources", "r4_openapi_duplicate_path.json").toFile(),
            OpenAPI.class);
    // Configure mock server to respond with appropriate example per metadata endpoint.
    mockServer
        .expect(ExpectedCount.once(), requestTo(new URI(R4_EXAMPLE_OPENAPI_ENDPOINT_1)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapperOpenapi.writeValueAsString(r4ExampleOpenapi1)));
    mockServer
        .expect(ExpectedCount.once(), requestTo(new URI(R4_EXAMPLE_OPENAPI_ENDPOINT_2)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapperOpenapi.writeValueAsString(r4ExampleDuplicatePathOpenapi)));
    // Run the unifier application using the R4 Openapi command line arguments.
    unifierApplication.run(new DefaultApplicationArguments(r4MetadataCommandLineArgs));
  }

  @Test(expected = OpenApiDuplicateKeyConflictException.class)
  @SneakyThrows
  public void openApiSchemaConflictTest() {
    // Test arguments to test R4 Metadata Unifier.
    final String[] r4MetadataCommandLineArgs =
        new String[] {
          ResourceTypeEnum.R4.type(),
          EndpointTypeEnum.OPENAPI.type(),
          R4_EXAMPLE_OPENAPI_ENDPOINT_1,
          R4_EXAMPLE_OPENAPI_ENDPOINT_2
        };
    final OpenAPI r4ExampleOpenapi1 =
        mapperOpenapi.readValue(
            Paths.get("src", "test", "resources", "r4_openapi_example_1.json").toFile(),
            OpenAPI.class);
    final OpenAPI r4ExampleConflictSchemaOpenapi =
        mapperOpenapi.readValue(
            Paths.get("src", "test", "resources", "r4_openapi_conflict_schema.json").toFile(),
            OpenAPI.class);
    // Configure mock server to respond with appropriate example per metadata endpoint.
    mockServer
        .expect(ExpectedCount.once(), requestTo(new URI(R4_EXAMPLE_OPENAPI_ENDPOINT_1)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapperOpenapi.writeValueAsString(r4ExampleOpenapi1)));
    mockServer
        .expect(ExpectedCount.once(), requestTo(new URI(R4_EXAMPLE_OPENAPI_ENDPOINT_2)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapperOpenapi.writeValueAsString(r4ExampleConflictSchemaOpenapi)));
    // Run the unifier application using the R4 Openapi command line arguments.
    unifierApplication.run(new DefaultApplicationArguments(r4MetadataCommandLineArgs));
  }

  @Test
  @SneakyThrows
  public void r4OpenapiUnifierTest() {
    // Test arguments to test R4 Metadata Unifier.
    final String[] r4MetadataCommandLineArgs =
        new String[] {
          ResourceTypeEnum.R4.type(),
          EndpointTypeEnum.OPENAPI.type(),
          R4_EXAMPLE_OPENAPI_ENDPOINT_1,
          R4_EXAMPLE_OPENAPI_ENDPOINT_2
        };
    // Load R4 Metadata examples and expected unified result from test resources.
    final OpenAPI r4ExampleOpenapiUnifiedExpected =
        mapperOpenapi.readValue(
            Paths.get("src", "test", "resources", "r4_openapi_example_unified.json").toFile(),
            OpenAPI.class);
    final OpenAPI r4ExampleOpenapi1 =
        mapperOpenapi.readValue(
            Paths.get("src", "test", "resources", "r4_openapi_example_1.json").toFile(),
            OpenAPI.class);
    final OpenAPI r4ExampleOpenapi2 =
        mapperOpenapi.readValue(
            Paths.get("src", "test", "resources", "r4_openapi_example_2.json").toFile(),
            OpenAPI.class);
    // Configure mock server to respond with appropriate example per metadata endpoint.
    mockServer
        .expect(ExpectedCount.once(), requestTo(new URI(R4_EXAMPLE_OPENAPI_ENDPOINT_1)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapperOpenapi.writeValueAsString(r4ExampleOpenapi1)));
    mockServer
        .expect(ExpectedCount.once(), requestTo(new URI(R4_EXAMPLE_OPENAPI_ENDPOINT_2)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapperOpenapi.writeValueAsString(r4ExampleOpenapi2)));
    // Run the unifier application using the R4 Openapi command line arguments.
    unifierApplication.run(new DefaultApplicationArguments(r4MetadataCommandLineArgs));
    // Obtain a unified R4 Openapi object via the mocked Amazon S3.
    final OpenAPI s3UnifiedOpenapi =
        mapperOpenapi.readValue(
            AmazonS3BucketUtilities.getResultFromS3(s3Client, amazonS3BucketConfig.getName()),
            OpenAPI.class);
    // Verify the result obtained from the mocked Amazon S3 matches the expected object.
    assertEquals(r4ExampleOpenapiUnifiedExpected, s3UnifiedOpenapi);
  }
}
