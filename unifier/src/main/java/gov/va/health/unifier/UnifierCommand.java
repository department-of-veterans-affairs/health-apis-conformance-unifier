package gov.va.health.unifier;

import gov.va.health.unifier.openapi.OpenApiV3Command;
import java.util.concurrent.Callable;
import picocli.AutoComplete.GenerateCompletion;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

@Command(
    name = "unify",
    mixinStandardHelpOptions = true,
    subcommands = {HelpCommand.class, GenerateCompletion.class, OpenApiV3Command.class})
public class UnifierCommand implements Callable<Integer> {

  @Spec CommandSpec spec;

  public static void main(String... args) {
    System.exit(mainNoExit(args));
  }

  public static int mainNoExit(String... args) {
    return new CommandLine(new UnifierCommand()).execute(args);
  }

  @Override
  public Integer call() throws Exception {
    throw new ParameterException(spec.commandLine(), "Missing required subcommand");
  }
}
