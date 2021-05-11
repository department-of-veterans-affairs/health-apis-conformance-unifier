package gov.va.health.unifier;

import gov.va.health.unifier.openapi.OpenApiV3UnifierCommand;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "unify", mixinStandardHelpOptions = true)
public class UnifierCommand implements Callable<Integer> {

  public static void main(String... args) {
    System.exit(mainNoExit(args));
  }

  public static int mainNoExit(String... args) {
    return new CommandLine(new UnifierCommand())
        .addSubcommand(new OpenApiV3UnifierCommand())
        .execute(args);
  }

  @Override
  public Integer call() throws Exception {
    return 0;
  }
}
