package gov.va.api.health.conformance.unifier.awss3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.aws.interfaces.s3.AmazonS3ClientServiceInterface;
import gov.va.api.health.dstu2.api.resources.Conformance;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AmazonS3ClientWriterServiceTest {

  private final String key = "key";

  private final String bucketName = "bucket-name";

  private final String contentType = "content-type";

  private final HashMap<String, String> metadataMap = new HashMap<>();

  private final AmazonS3BucketConfig config = new AmazonS3BucketConfig();

  @Captor ArgumentCaptor<PutObjectRequest> argument;

  private ObjectMapper mapper = new ObjectMapper();

  @Mock private AmazonS3ClientServiceInterface s3ClientService;

  @Mock private AmazonS3 s3Client;

  private Conformance conformance;

  private AmazonS3ClientWriterService service;

  @Before
  public void init() throws Exception {
    MockitoAnnotations.initMocks(this);
    config.setName(bucketName);
    conformance =
        mapper.readValue(
            Paths.get("src", "test", "resources", "dstu2_example_metadata_unified.json").toFile(),
            Conformance.class);
    service = new AmazonS3ClientWriterService(config, s3ClientService);
    when(s3ClientService.s3Client()).thenReturn(s3Client);
    when(s3Client.doesBucketExistV2(config.getName())).thenReturn(true);
  }

  /** Verify the bucket name written to S3 matches the expected value. */
  @Test
  public void writeToBucketVerifyBucketName() throws Exception {
    service.writeToBucket(key, metadataMap, conformance, contentType);
    verify(s3Client).putObject(argument.capture());
    assertEquals(bucketName, argument.getValue().getBucketName());
  }

  /** Verify that the content written to S3 contains no tabs or spaces. */
  @Test
  public void writeToBucketVerifyContent() throws Exception {
    service.writeToBucket(key, metadataMap, conformance, contentType);
    verify(s3Client).putObject(argument.capture());
    assertThat(IOUtils.toString(argument.getValue().getInputStream(), StandardCharsets.UTF_8))
        .withFailMessage("Tabs and newlines break our customer.")
        .doesNotContain("\n", "\t");
  }

  /** Verify the content-type written to S3 matches the expected value. */
  @Test
  public void writeToBucketVerifyContentType() throws Exception {
    service.writeToBucket(key, metadataMap, conformance, contentType);
    verify(s3Client).putObject(argument.capture());
    assertEquals(contentType, argument.getValue().getMetadata().getContentType());
  }
}
