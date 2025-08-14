package com.powergem.wce.commands;

import com.powergem.sql.UncheckedConnection;
import com.powergem.wce.Importer;
import com.powergem.wce.ScenariosTable;
import com.powergem.worstcasetrlim.model.WcResult;
import com.powergem.worstcasetrlim.model.WorstCaseTrLim;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import picocli.CommandLine;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static com.powergem.wce.Utils.getConnection;
import static com.powergem.wce.commands.Fuzz.toWcResult;

@CommandLine.Command(
        name = "reindex",
        description = "Re-indexes the 'id' attribute of wcResult and each flowgate.",
        usageHelpWidth = 132
)
public final class ReindexCommand implements Callable<Integer> {
  @CommandLine.Option(names = {"-i", "--input"}, description = "The JSON file to inspect.", defaultValue = "WClusterTrLimSumJson.json")
  private Path jsonFile = Path.of("WClusterTrLimSumJson.json");

  @CommandLine.Option(names = {"-n", "--no-ansi"}, description = "Do not use ANSI codes in the output")
  private boolean noAnsi = false;

  @Override
  public Integer call() throws Exception {
    System.setProperty("wce.useAnsi", String.valueOf(!noAnsi));

    this.jsonFile = this.jsonFile.normalize().toAbsolutePath();

//    String jdbcUrl = "jdbc:sqlite::memory:";
    if (Files.exists(Paths.get("reindex.db"))) {
      Files.delete(Paths.get("reindex.db"));
    }
    String jdbcUrl = "jdbc:sqlite:reindex.db";
    try (Connection connection = getConnection(jdbcUrl)) {
      connection.setAutoCommit(false);

      System.err.printf("[%tT] Loading %s...%n", LocalTime.now(), jsonFile);
      Importer.importData(this.jsonFile, connection);

      List<WcResult> wcResults = new ArrayList<>();
      try (UncheckedConnection uncheckedConnection = new UncheckedConnection(connection)) {
        try (ScenariosTable scenariosTable = new ScenariosTable(uncheckedConnection)) {
          scenariosTable.reIndexScenarios();
          scenariosTable.getScenarioTables().forEach(scenarioTable -> {
            WcResult wcResult = toWcResult(scenarioTable);
            wcResults.add(wcResult);
          });
          scenariosTable.reIndexScenarios();
        }
      }

      WorstCaseTrLim worstCaseTrLim = new WorstCaseTrLim(wcResults);

      Jsonb jsonb = Jsonb.builder().build();
      JsonType<WorstCaseTrLim> worstCaseTrLimJsonType = jsonb.type(WorstCaseTrLim.class);

    try (OutputStream inputStream = Files.newOutputStream(Paths.get("reindex.json"))) {
      worstCaseTrLimJsonType.toJson(worstCaseTrLim, inputStream);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
//      worstCaseTrLimJsonType.toJson(worstCaseTrLim, System.out);

      return 0;
    }
  }
}
