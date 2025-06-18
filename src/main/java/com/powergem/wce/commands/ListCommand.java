package com.powergem.wce.commands;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "list",
        subcommands = {
                ListScenariosCommand.class,
                ListUnlocatedCommand.class,
                ListBusCommand.class,
                ListFlowgateCommand.class,
                ListBusesCommand.class,
        },
        usageHelpWidth = 132
)
public final class ListCommand implements Callable<Integer> {
  @CommandLine.Spec
  CommandLine.Model.CommandSpec spec;

  @Override
  public Integer call() {
    throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand");
  }
}
