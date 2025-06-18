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
  @CommandLine.Parameters(index = "0", description = "The JSON file to inspect.", defaultValue = "WClusterTrLimSumJson.json")
  private Path jsonFile;

  @CommandLine.Parameters(index = "1", description = "The id of the flowgate to list.")
  private int flowgateId;

  @Override
  public Integer call() throws Exception {
    String jdbcUrl = "jdbc:sqlite::memory:";

    int scenarioId = 1;

    try (Connection connection = getConnection(jdbcUrl)) {
      Importer.importData(this.jsonFile, connection);

      DataFile dataFile = new DataFile(new UncheckedConnection(connection));
      dataFile.getFlowgateById(this.flowgateId, scenarioId).ifPresent(entity -> {
        dumpFlowgate(entity, dataFile, scenarioId, 0);
      });
    }

    return 0;
  }
}
