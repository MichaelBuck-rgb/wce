package com.powergem.wce;

import com.powergem.wce.commands.*;
import picocli.CommandLine;

@CommandLine.Command(
        name = "wce",
        description = "A utility to inspect a WClusterTrLimSumJson.json file",
        subcommands = {
                ExtractCommand.class,
                SqliteCommand.class,
                ListCommand.class,
                InfoCommand.class,
                ReindexCommand.class,
                SearchCommand.class,
                Fuzz.class
        },
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        usageHelpWidth = 132
)
public class Main {

  @SuppressWarnings("InstantiationOfUtilityClass")
  public static void main(String[] args) {
    int exitCode = new CommandLine(new Main())
            .setCaseInsensitiveEnumValuesAllowed(true)
            .setUsageHelpAutoWidth(true)
            .execute(args);
    System.exit(exitCode);
  }

}
