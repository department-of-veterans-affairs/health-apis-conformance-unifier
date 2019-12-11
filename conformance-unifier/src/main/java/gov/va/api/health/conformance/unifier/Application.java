package gov.va.api.health.conformance.unifier;

import gov.va.api.health.conformance.unifier.fhir.ResourceTypeEnum;
import gov.va.api.health.conformance.unifier.fhir.dstu2.Dstu2UnifierService;
import gov.va.api.health.conformance.unifier.fhir.r4.R4UnifierService;
import gov.va.api.health.conformance.unifier.fhir.stu3.Stu3UnifierService;
import gov.va.views.amazon.s3.AmazonS3ClientServiceConfig;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
        r4UnifierService.unify(ResourceTypeEnum.R4, endpointType, urlList);
        break;
      case DSTU2:
        dstu2UnifierService.unify(ResourceTypeEnum.DSTU2, endpointType, urlList);
        break;
      case STU3:
        stu3UnifierService.unify(ResourceTypeEnum.STU3, endpointType, urlList);
        break;
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
