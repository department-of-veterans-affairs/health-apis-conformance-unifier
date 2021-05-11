package gov.va.health.unifier;

import lombok.AccessLevel;
import lombok.Getter;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

/** This is a command for items that require a subcommand. */
public class CommandWithRequiredSubcommand implements Runnable {
  @Spec
  @Getter(AccessLevel.PROTECTED)
  private CommandSpec spec;

  @Override
  public void run() {
    throw new ParameterException(spec.commandLine(), "Missing required subcommand");
  }
}
