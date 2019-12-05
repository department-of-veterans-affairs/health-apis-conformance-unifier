package gov.va.api.health.conformance.unifier.awss3;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration
@ConfigurationProperties("result")
@Data
public class AmazonS3BucketConfig implements InitializingBean {

  /** Required. */
  private String bucket;

  @Override
  public void afterPropertiesSet() throws Exception {
    // Required properties check.
    Assert.notNull(bucket, "AmazonS3BucketConfig bucket must not be null.");
  }
}
