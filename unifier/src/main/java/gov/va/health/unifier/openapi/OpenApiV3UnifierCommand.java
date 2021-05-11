package gov.va.health.unifier.openapi;

import static gov.va.health.unifier.Print.println;
import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.health.unifier.openapi.MergeConfig.Contributor;
import java.io.File;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "openapi")
public class OpenApiV3UnifierCommand implements Callable<Integer> {

  @Option(
      names = {"-c", "--config"},
      required = true,
      defaultValue = "merge-openapi.json")
  File configFile;

  @Override
  public Integer call() throws Exception {
    println("Loading %s", configFile);
    var mapper = new ObjectMapper();
    var config = mapper.readValue(configFile, MergeConfig.class);
    println("%s", config);
    var sources = config.in().stream().map(Contributor::asOpenApiV3Source).collect(toList());
    var merged =
        OpenApiV3Unifier.startingWith(InitialOpenApiV3.of(config.properties())).apply(sources);
    return 0;
  }
}
