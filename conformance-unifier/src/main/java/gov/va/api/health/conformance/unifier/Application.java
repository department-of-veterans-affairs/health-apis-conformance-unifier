package gov.va.api.health.conformance.unifier;

import gov.va.api.health.conformance.unifier.exception.AmazonS3InterfaceException;
import gov.va.api.health.conformance.unifier.exception.DuplicateCapabilityResourceException;
import gov.va.api.health.conformance.unifier.fhir.ResourceTypeEnum;
import gov.va.api.health.conformance.unifier.fhir.dstu2.Dstu2UnifierService;
import gov.va.api.health.conformance.unifier.fhir.r4.R4UnifierService;
import gov.va.api.health.conformance.unifier.fhir.stu3.Stu3UnifierService;
import gov.va.api.health.informational.dstu2.conformance.ConformanceStatementProperties;
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

  /** DSTU2 unifier service. */
  @Autowired Dstu2UnifierService dstu2UnifierService;

  /** STU3 unifier service. */
  @Autowired Stu3UnifierService stu3UnifierService;

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

  /**
   * Override the configuration properties prefix by defining the dstu2 bean instead of importing
   * the configuration class directly.
   *
   * @return ConformanceStatementProperties.
   */
  @Bean
  @ConfigurationProperties(prefix = "dstu2.conformance")
  public ConformanceStatementProperties dstu2ConformanceStatementPropertiesConfig() {
    return new ConformanceStatementProperties();
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
    final String endpointType = argList.get(ArgEnum.ENDPOINT.ordinal());
    final List<String> urlList = argList.subList(ArgEnum.URL.ordinal(), argList.size());
    switch (ResourceTypeEnum.fromType(resourceType)) {
      case R4:
        r4UnifierService.unify(endpointType, urlList);
        break;
      case DSTU2:
        dstu2UnifierService.unify(endpointType, urlList);
        break;
      case STU3:
        stu3UnifierService.unify(endpointType, urlList);
        break;
      default:
        throw new IllegalArgumentException("Unsupported resource type: " + resourceType);
    }
  }

  /**
   * Override the configuration properties prefix by defining the stu3 bean instead of importing the
   * configuration class directly.
   *
   * @return CapabilityStatementProperties.
   */
  @Bean
  @ConfigurationProperties(prefix = "stu3.capability")
  public gov.va.api.health.informational.stu3.capability.CapabilityStatementProperties
      stu3CapabilityStatementPropertiesConfig() {
    return new gov.va.api.health.informational.stu3.capability.CapabilityStatementProperties();
  }

  /** Expected argument order. */
  public enum ArgEnum {
    RESOURCE,
    ENDPOINT,
    URL
  }
}
