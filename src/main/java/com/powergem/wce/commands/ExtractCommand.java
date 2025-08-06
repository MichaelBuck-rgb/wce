package com.powergem.wce.commands;

import com.powergem.sql.UncheckedConnection;
import com.powergem.wce.DataFile;
import com.powergem.wce.Importer;
import picocli.CommandLine;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.Callable;

import static com.powergem.wce.Utils.getConnection;

@CommandLine.Command(
        name = "extract",
        description = "Extracts the specified buses to the screen.",
        usageHelpWidth = 132
)
public final class ExtractCommand implements Callable<Integer> {
  @CommandLine.Parameters(index = "0", description = "List of Bus numbers.")
  private List<Integer> buses;

  @CommandLine.Option(names = {"-i", "--input"}, description = "The JSON file to inspect.", defaultValue = "WClusterTrLimSumJson.json")
  private Path jsonFile = Path.of("WClusterTrLimSumJson.json");

//  @CommandLine.Option(names = {"-s", "--scenario"}, description = "The ID of the scenario to get the bus from.", defaultValue = "1")
//  private int scenarioId = 1;

  @CommandLine.Option(names = {"-na", "--no-ansi"}, description = "Do not use ANSI codes in the output")
  private boolean noAnsi = false;

  //  @CommandLine.Option(names = {"-X", "--exclude"}, description = "Objects to exclude from the list.")
//  private String exclude = "";

  @Override
  public Integer call() throws Exception {
    System.setProperty("wce.useAnsi", String.valueOf(!noAnsi));

//    Set<ReportType> exclusions = Utilities.toExclusions(this.exclude);

    String jdbcUrl = "jdbc:sqlite::memory:";

    try (Connection connection = getConnection(jdbcUrl)) {
      Importer.importData(this.jsonFile, connection);
      try (UncheckedConnection uncheckedConnection = new UncheckedConnection(connection)) {
        DataFile dataFile = new DataFile(uncheckedConnection);
        dataFile.extractBuses(this.buses, uncheckedConnection);
      }
    }

    return 0;
  }
}
