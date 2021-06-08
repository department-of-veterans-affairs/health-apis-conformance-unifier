package gov.va.api.health.conformance.unifier.awss3;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

/** S3 bucket config. */
@Configuration
@ConfigurationProperties("bucket")
@Data
public class AmazonS3BucketConfig implements InitializingBean {

  /** Required. */
  private String name;

  /** Optional. */
  private boolean create = false;

  @Override
  public void afterPropertiesSet() throws Exception {
    // Required properties check.
    Assert.notNull(name, "AmazonS3BucketConfig name must not be null.");
  }
}
