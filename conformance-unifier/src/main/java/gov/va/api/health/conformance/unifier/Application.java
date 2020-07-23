package gov.va.api.health.conformance.unifier;

import gov.va.api.health.aws.interfaces.s3.AmazonS3ClientServiceConfig;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({AmazonS3ClientServiceConfig.class})
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
    List<String> argList = args.getNonOptionArgs();
    // With the java 14 upgrade this is coming in as a single entry with a comma separated string
    if (argList.size() == 1) {
      argList = Arrays.asList(argList.get(0).split(","));
    }
    // Check count of required non option args.
    if (argList.size() < ArgEnum.values().length) {
      throw new IllegalArgumentException(
          "Invalid number of arguments.  Expected minimum count " + ArgEnum.values().length + ".");
    }

    // Process option metadata arg.
    // Arg may contain one or more key=value separated by comma.
    final List<String> metadataValueList = args.getOptionValues(METADATA_ARG);
    Map<String, String> metadataMap = new HashMap<>();
    if (metadataValueList != null) {
      try {
        metadataValueList.stream()
            .map(metadataKeyValueString -> metadataKeyValueString.split("\\s*,\\s*"))
            .forEachOrdered(
                metadataKeyValueArray -> {
                  for (String keyValueString : metadataKeyValueArray) {
                    final String[] keyValuePair = keyValueString.split("=", 2);
                    metadataMap.put(keyValuePair[0], keyValuePair[1]);
                  }
                });
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
