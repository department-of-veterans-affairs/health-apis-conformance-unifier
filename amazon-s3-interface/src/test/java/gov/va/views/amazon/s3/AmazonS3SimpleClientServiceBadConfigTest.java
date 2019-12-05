package gov.va.views.amazon.s3;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/** Forcefully test a badly configured simple client. */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  classes = {AmazonS3SimpleClientServiceBadConfigTest.TestConfiguration.class},
  initializers = ConfigFileApplicationContextInitializer.class
)
public class AmazonS3SimpleClientServiceBadConfigTest {

  @Autowired private AmazonS3ClientConfig config;

  @Test(expected = IllegalArgumentException.class)
  public void testBadConfig() {
    config.setServiceEndpoint(null);
    config.setClientRegion(null);
    AmazonS3SimpleClientService service = new AmazonS3SimpleClientService(config);
    service.afterPropertiesSet();
  }

  /** Load test context with just the AmazonS3ClientConfig. */
  @EnableAutoConfiguration
  @EnableConfigurationProperties(value = {AmazonS3ClientConfig.class})
  public static class TestConfiguration {}
}
