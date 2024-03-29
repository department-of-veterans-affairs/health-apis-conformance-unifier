package gov.va.api.health.conformance.unifier.awss3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.aws.interfaces.s3.AmazonS3ClientServiceInterface;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service to facilitate writing unified results to Amazon S3. */
@Service
@AllArgsConstructor(onConstructor = @__({@Autowired}))
@Slf4j
public class AmazonS3ClientWriterService {

  private final AmazonS3BucketConfig bucketConfig;

  @Setter private AmazonS3ClientServiceInterface s3ClientService;

  /**
   * Write object to the Amazon S3 Bucket.Any exceptions with the interface will ripple up.
   *
   * @param key Name of object in bucket.
   * @param metadataMap Map of metadata to associate with the generated S3 object.
   * @param object Object to write.
   * @param contentType The content type for metadata
   */
  public void writeToBucket(
      final String key,
      final Map<String, String> metadataMap,
      final Object object,
      final String contentType) {
    writeToBucket(key, metadataMap, object, contentType, JacksonConfig.createMapper());
  }

  /**
   * Write object to the Amazon S3 Bucket.Any exceptions with the interface will ripple up.
   *
   * @param key Name of object in bucket.
   * @param metadataMap Map of metadata to associate with the generated S3 object.
   * @param object Object to write.
   * @param contentType The content type for metadata
   * @param mapper Jackson mapper used to serialize.
   */
  @SneakyThrows
  public void writeToBucket(
      final String key,
      final Map<String, String> metadataMap,
      final Object object,
      final String contentType,
      final ObjectMapper mapper) {

    // Write the object to AWS
    AmazonS3 s3Client = s3ClientService.s3Client();

    if (!s3Client.doesBucketExistV2(bucketConfig.getName())) {
      if (bucketConfig.isCreate()) {
        log.info("{} does not exist.  Creating it.", bucketConfig.getName());
        s3Client.createBucket(bucketConfig.getName());
      } else {
        throw new RuntimeException(bucketConfig.getName() + " does not exist.");
      }
    }

    final String unifiedResult = mapper.writeValueAsString(object);
    log.info("Storing unified result {} to AWS S3 {}.", key, bucketConfig.getName());

    // Upload a text string as a new object as input stream with metadata.
    ObjectMetadata metadata = new ObjectMetadata();
    final byte[] contentAsBytes = unifiedResult.getBytes(StandardCharsets.UTF_8);
    metadata.setContentLength(contentAsBytes.length);
    metadata.setContentType(contentType);
    metadataMap.forEach((k, v) -> metadata.addUserMetadata(k, v));
    try (ByteArrayInputStream contentsAsStream = new ByteArrayInputStream(contentAsBytes)) {
      s3Client.putObject(
          new PutObjectRequest(bucketConfig.getName(), key, contentsAsStream, metadata));
    }
  }
}
