package gov.va.health.unifier.openapi;

import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

@Command(
    name = "openapi",
    description = "Work with Open API V3 specifications",
    subcommands = {OpenApiV3MergeCommand.class, ConfigSampleCommand.class})
public class OpenApiV3Command implements Callable<Integer> {
  @Spec CommandSpec spec;

  @Override
  public Integer call() throws Exception {
    throw new ParameterException(spec.commandLine(), "Missing required subcommand");
  }
}
