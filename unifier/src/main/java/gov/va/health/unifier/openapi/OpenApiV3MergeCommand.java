package gov.va.health.unifier.openapi;

import static gov.va.health.unifier.Print.println;
import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import java.io.File;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;

@Command(name = "merge", description = "Merge multiple Open API V3 specifications")
public class OpenApiV3MergeCommand implements Callable<Integer> {

  @Option(
      names = {"-c", "--config"},
      required = true,
      description = "Merge configuration file.",
      scope = ScopeType.LOCAL)
  File configFile;

  @Override
  public Integer call() throws Exception {
    println("Loading %s", configFile);
    var mapper = new ObjectMapper();
    var config = mapper.readValue(configFile, MergeConfig.class);
    var sources = config.in().stream().map(OpenApiV3Source::from).collect(toList());
    var merged =
        OpenApiV3Unifier.startingWith(InitialOpenApiV3.of(config.properties())).apply(sources);
    Json.pretty().writeValue(new File(config.out()), merged);
    return 0;
  }
}
