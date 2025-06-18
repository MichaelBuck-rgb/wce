package com.powergem.wce.commands;

import com.powergem.sql.UncheckedConnection;
import com.powergem.wce.DataFile;
import com.powergem.wce.Importer;
import picocli.CommandLine;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.concurrent.Callable;

import static com.powergem.wce.Utilities.dumpFlowgate;
import static com.powergem.wce.Utils.getConnection;

@CommandLine.Command(name = "flowgate")
public final class ListFlowgateCommand implements Callable<Integer> {
  @CommandLine.Parameters(index = "0", description = "The id of the flowgate to list.")
  private int flowgateId;

  @CommandLine.Option(names = {"-i", "--input"}, description = "The JSON file to inspect.", defaultValue = "WClusterTrLimSumJson.json")
  private Path jsonFile = Path.of("WClusterTrLimSumJson.json");

  @CommandLine.Option(names = {"-s", "--scenario"}, description = "The ID of the scenario to get the bus from.", defaultValue = "1")
  private int scenarioId = 1;

  @CommandLine.Option(names = {"-na", "--no-ansi"}, description = "Do not use ANSI codes in the output")
  private boolean noAnsi = false;

  @Override
  public Integer call() throws Exception {
    System.setProperty("wce.useAnsi", String.valueOf(!noAnsi));

    String jdbcUrl = "jdbc:sqlite::memory:";

    try (Connection connection = getConnection(jdbcUrl)) {
      Importer.importData(this.jsonFile, connection);

      DataFile dataFile = new DataFile(new UncheckedConnection(connection));
      dataFile.getFlowgateById(this.flowgateId, scenarioId).ifPresent(entity -> dumpFlowgate(entity, dataFile, scenarioId, 0));
    }

    return 0;
  }
}
