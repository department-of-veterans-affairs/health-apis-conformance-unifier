package gov.va.api.health.conformance.unifier.awss3;

import com.amazonaws.services.s3.AmazonS3;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.conformance.unifier.exception.AmazonS3InterfaceException;
import gov.va.views.amazon.s3.AmazonS3ClientServiceInterface;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service to facilitate writing unified results to Amazon S3. */
@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
@Slf4j
public class AmazonS3ClientWriterService {

  public static final String KEY_DATE_FORMAT = "yyyy_MM_dd_HH_mm_ss";

  private final AmazonS3BucketConfig bucketConfig;

  private final AmazonS3ClientServiceInterface s3ClientService;

  /**
   * Write object to the Amazon S3 Bucket.
   *
   * @param object Object to write.
   */
  public void writeToBucket(final Object object) {

    // TODO: figure out unique key.
    String key = new SimpleDateFormat(KEY_DATE_FORMAT).format(new Date());

    // Write the object to AWS
    try {
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

    } catch (Exception e) {

      // Intentionally catch any exception at this point to ensure if any error with Amazon is
      // encountered then
      // the errors are at least logged and the application exits cleanly.
      // The call was transmitted successfully, but Amazon S3 couldn't process
      // it, so it returned an error response.
      // Amazon S3 couldn't be contacted for a response, or the client
      // couldn't parse the response from Amazon S3.
      log.error("Problem writing unified result to awss3: " + e.getMessage());
      throw new AmazonS3InterfaceException(
          "Problem writing unified result to awss3: " + e.getMessage());
    }
  }
}
