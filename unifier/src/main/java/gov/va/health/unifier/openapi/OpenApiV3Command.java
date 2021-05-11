package gov.va.health.unifier.openapi;

import gov.va.health.unifier.CommandWithRequiredSubcommand;
import picocli.CommandLine.Command;

@Command(
    name = "openapi",
    description = "Work with Open API V3 specifications",
    subcommands = {OpenApiV3MergeCommand.class, ConfigSampleCommand.class})
public class OpenApiV3Command extends CommandWithRequiredSubcommand {}
