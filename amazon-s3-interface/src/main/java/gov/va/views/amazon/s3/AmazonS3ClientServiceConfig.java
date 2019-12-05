package gov.va.views.amazon.s3;

import org.springframework.context.annotation.Import;

/**
 * Configuration class to instantiate the appropriate Amazon S3 Client based on the application
 * property amazon.s3.sessionClient.
 */
@Import({
  AmazonS3ClientConfig.class,
  AmazonS3SimpleClientService.class,
  AmazonS3SessionClientService.class
})
public class AmazonS3ClientServiceConfig {}
