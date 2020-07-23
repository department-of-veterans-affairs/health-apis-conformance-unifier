package gov.va.api.health.conformance.unifier;

import gov.va.api.health.aws.interfaces.s3.AmazonS3ClientServiceConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({AmazonS3ClientServiceConfig.class})
@CommonsLog
public class Application implements ApplicationRunner {

  /** Optional argument to associate metadata with the generated S3 object. */
  public static final String METADATA_ARG = "metadata";

  /** Unifier service. */
  @Autowired private UnifierService unifierService;

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
    // Check count of required non option args.
    List<String> argList = args.getNonOptionArgs();
    if (argList.size() < ArgEnum.values().length) {
      log.error("argList:" + argList);
      argList.forEach(arg -> log.error("arg:" + arg));
      throw new IllegalArgumentException(
          "Invalid number of arguments.  Expected minimum count " + ArgEnum.values().length + ".");
    }

    // Process option metadata arg.
    // Arg may contain one or more key=value separated by comma.
    final List<String> metadataValueList = args.getOptionValues(METADATA_ARG);
    Map<String, String> metadataMap = new HashMap<>();
    if (metadataValueList != null) {
      try {
        for (String metadataKeyValueString : metadataValueList) {
          final String[] metadataKeyValueArray = metadataKeyValueString.split("\\s*,\\s*");
          for (String keyValueString : metadataKeyValueArray) {
            final String[] keyValuePair = keyValueString.split("=", 2);
            metadataMap.put(keyValuePair[0], keyValuePair[1]);
          }
        }
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid metadata argument: " + e.getMessage());
      }
    }

    // Call unifier service.
    unifierService.unify(
        argList.get(ArgEnum.RESOURCE.ordinal()),
        argList.get(ArgEnum.ENDPOINT.ordinal()),
        argList.subList(ArgEnum.URL.ordinal(), argList.size()),
        metadataMap);
  }

  /** Expected argument order. */
  public enum ArgEnum {
    RESOURCE,
    ENDPOINT,
    URL
  }
}
