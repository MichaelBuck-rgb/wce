package com.powergem.wce.commands;

import com.powergem.sql.UncheckedConnection;
import com.powergem.wce.DataFile;
import com.powergem.wce.Importer;
import picocli.CommandLine;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.concurrent.Callable;

import static com.powergem.wce.Utils.getConnection;

@CommandLine.Command(name = "scenarios")
public final class ListScenariosCommand implements Callable<Integer> {
  @CommandLine.Option(names = {"-i", "--input"}, description = "The JSON file to inspect.", defaultValue = "WClusterTrLimSumJson.json")
  private Path jsonFile = Path.of("WClusterTrLimSumJson.json");

  @CommandLine.Option(names = {"-na", "--no-ansi"}, description = "Do not use ANSI codes in the output")
  private boolean noAnsi = false;

  @Override
  public Integer call() throws Exception {
    System.setProperty("wce.useAnsi", String.valueOf(!noAnsi));

    String jdbcUrl = "jdbc:sqlite::memory:";

    try (Connection connection = getConnection(jdbcUrl)) {
      Importer.importData(this.jsonFile, connection);

      DataFile dataFile = new DataFile(new UncheckedConnection(connection));
      dataFile.getScenarios().forEach(System.out::println);
    }

    return 0;
  }
}
