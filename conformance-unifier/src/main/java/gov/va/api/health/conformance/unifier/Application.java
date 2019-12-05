package gov.va.api.health.conformance.unifier;

import gov.va.api.health.conformance.unifier.exception.AmazonS3InterfaceException;
import gov.va.api.health.conformance.unifier.exception.DuplicateCapabilityResourceException;
import gov.va.api.health.conformance.unifier.fhir.ResourceTypeEnum;
import gov.va.api.health.conformance.unifier.fhir.r4.R4UnifierService;
import gov.va.api.health.informational.r4.capability.CapabilityStatementProperties;
import gov.va.views.amazon.s3.AmazonS3ClientServiceConfig;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.ExitCodeExceptionMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({AmazonS3ClientServiceConfig.class})
@Slf4j
public class Application implements ApplicationRunner {

  /** R4 unifier service. */
  @Autowired R4UnifierService r4UnifierService;

  /**
   * Main.
   *
   * @param args Arguments.
   */
  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(Application.class);
    app.setBannerMode(Banner.Mode.OFF);
    app.run(args);
  }

  @Bean
  ExitCodeExceptionMapper exitCodeExceptionMapper() {
    return exception -> {
      log.error("Exception occurred: " + exception.getMessage());
      if (exception.getCause() instanceof IllegalArgumentException) {
        return 2;
      }
      if (exception.getCause() instanceof DuplicateCapabilityResourceException) {
        return 3;
      }
      if (exception.getCause() instanceof AmazonS3InterfaceException) {
        return 4;
      }
      return 1;
    };
  }

  /**
   * Override the configuration properties prefix by defining the r4 bean instead of importing the
   * configuration class directly.
   *
   * @return CapabilityStatementProperties.
   */
  @Bean
  @ConfigurationProperties(prefix = "r4.capability")
  public CapabilityStatementProperties r4CapabilityStatementPropertiesConfig() {
    return new CapabilityStatementProperties();
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    List<String> argList = args.getNonOptionArgs();
    if (argList.size() < ArgEnum.values().length) {
      throw new IllegalArgumentException(
          "Invalid number of arguments.  Expected minimum count " + ArgEnum.values().length + ".");
    }
    // Call appropriate unifier based on the resource type argument.
    final String resourceType = argList.get(ArgEnum.RESOURCE.ordinal());
    switch (ResourceTypeEnum.fromType(resourceType)) {
      case R4:
        r4UnifierService.unify(
            argList.get(ArgEnum.ENDPOINT.ordinal()),
            argList.subList(ArgEnum.URL.ordinal(), argList.size()));
        break;
      case DSTU2:
      case STU3:
      default:
        throw new IllegalArgumentException("Unsupported resource type: " + resourceType);
    }
  }

  /** Expected argument order. */
  public enum ArgEnum {
    RESOURCE,
    ENDPOINT,
    URL
  }
}
