package gov.va.api.health.conformance.unifier.awss3;

import com.amazonaws.services.s3.AmazonS3;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.views.amazon.s3.AmazonS3ClientServiceInterface;
import java.text.SimpleDateFormat;
import java.util.Date;
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

  public static final String KEY_DATE_FORMAT = "yyyy_MM_dd_HH_mm_ss";

  private final AmazonS3BucketConfig bucketConfig;

  @Setter private AmazonS3ClientServiceInterface s3ClientService;

  /**
   * Write object to the Amazon S3 Bucket. Any exceptions with the interface will ripple up.
   *
   * @param object Object to write.
   */
  @SneakyThrows
  public void writeToBucket(final Object object) {

    // TODO: figure out unique key.
    String key = new SimpleDateFormat(KEY_DATE_FORMAT).format(new Date());

    // Write the object to AWS
    AmazonS3 s3Client = s3ClientService.s3Client();

    if (!s3Client.doesBucketExistV2(bucketConfig.getBucket())) {
      log.info(bucketConfig.getBucket() + " does not exist.  Creating it.");
      s3Client.createBucket(bucketConfig.getBucket());
    }

    final String unifiedResult =
        JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
    log.info("Storing unified result " + key + " to AWS S3: " + unifiedResult);
    // Upload a text string as a new object.
    s3Client.putObject(bucketConfig.getBucket(), key, unifiedResult);
  }
}
