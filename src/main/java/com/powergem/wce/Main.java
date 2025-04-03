package com.powergem.wce;

import com.powergem.wce.commands.InfoCommand;
import com.powergem.wce.commands.NormalizeCommand;
import com.powergem.wce.commands.SqliteCommand;
import com.powergem.wce.commands.ListCommand;
import picocli.CommandLine;

@CommandLine.Command(
        name = "wce",
        description = "A utility to inspect a WClusterTrLimSumJson.json file",
        subcommands = {
                SqliteCommand.class,
                ListCommand.class,
                InfoCommand.class,
                NormalizeCommand.class,
        },
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        usageHelpWidth = 132
)
public class Main {

  @SuppressWarnings("InstantiationOfUtilityClass")
  public static void main(String[] args) {
    int exitCode = new CommandLine(new Main()).execute(args);
    System.exit(exitCode);
  }

}
