package gov.va.health.unifier;

import gov.va.health.unifier.openapi.OpenApiV3Command;
import picocli.AutoComplete.GenerateCompletion;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

@Command(
    name = "unifier",
    mixinStandardHelpOptions = true,
    subcommands = {HelpCommand.class, GenerateCompletion.class, OpenApiV3Command.class})
public class UnifierCommand extends CommandWithRequiredSubcommand {

  public static void main(String... args) {
    System.exit(mainNoExit(args));
  }

  public static int mainNoExit(String... args) {
    return new CommandLine(new UnifierCommand()).execute(args);
  }
}
