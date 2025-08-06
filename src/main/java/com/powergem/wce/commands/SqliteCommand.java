package com.powergem.wce.commands;

import com.powergem.wce.Importer;
import picocli.CommandLine;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.concurrent.Callable;

import static com.powergem.wce.Utils.getConnection;

@CommandLine.Command(
        name = "sqlite",
        description = "Converts the specified WClusterTrLimSumJson.json into constituent tables in a SQLite database.",
        usageHelpWidth = 132
)
public class SqliteCommand implements Callable<Integer> {
  @CommandLine.Option(names = {"-i", "--input"}, description = "The JSON file to inspect.", defaultValue = "WClusterTrLimSumJson.json")
  private Path jsonFile = Path.of("WClusterTrLimSumJson.json");

//  @CommandLine.Option(names = {"-s", "--scenario"}, description = "The ID of the scenario to get the bus from.", defaultValue = "1")
//  private int scenarioId = 1;

  @CommandLine.Option(names = {"-n", "--no-ansi"}, description = "Do not use ANSI codes in the output")
  private boolean noAnsi = false;

  @Override
  public Integer call() throws Exception {
    System.setProperty("wce.useAnsi", String.valueOf(!noAnsi));

    String jdbcUrl = "jdbc:sqlite:WClusterTrLimSumJson.sqlite";

    try (Connection connection = getConnection(jdbcUrl)) {
      Importer.importData(this.jsonFile, connection);
    }

    return 0;
  }
}
