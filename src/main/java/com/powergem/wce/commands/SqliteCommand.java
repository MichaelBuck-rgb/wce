package com.powergem.wce.commands;

import com.powergem.wce.Importer;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.util.concurrent.Callable;

import static com.powergem.wce.Utils.getConnection;

@CommandLine.Command(
        name = "sqlite",
        description = "Converts the specified WClusterTrLimSumJson.json into constituent tables in a SQLite database.",
        usageHelpWidth = 132
)
public final class SqliteCommand implements Callable<Integer> {
  @CommandLine.Option(names = {"-i", "--input"}, description = "The JSON file to inspect.", defaultValue = "WClusterTrLimSumJson.json")
  private Path jsonFile = Path.of("WClusterTrLimSumJson.json");

//  @CommandLine.Option(names = {"-s", "--scenario"}, description = "The ID of the scenario to get the bus from.", defaultValue = "1")
//  private int scenarioId = 1;

  @CommandLine.Option(names = {"-f", "--force"}, description = "Overwrite existing file.")
  private boolean force = false;

  @CommandLine.Option(names = {"-n", "--no-ansi"}, description = "Do not use ANSI codes in the output")
  private boolean noAnsi = false;

  @Override
  public Integer call() throws Exception {
    System.setProperty("wce.useAnsi", String.valueOf(!noAnsi));

    this.jsonFile = this.jsonFile.normalize().toAbsolutePath();

    String baseName = this.jsonFile.getFileName().toString();
    int indexOf = baseName.lastIndexOf('.');
    Path sqliteFile = Paths.get(baseName.substring(0, indexOf) + ".db");

    if (!this.force && Files.exists(sqliteFile)) {
      System.err.println("File " + sqliteFile + " already exists.  Add --force to overwrite.");
      return 1;
    }

    Path tempFile = Files.createTempFile(baseName, null);

    System.out.println("Converting " + this.jsonFile + " to " + sqliteFile);

    String jdbcUrl = "jdbc:sqlite:" + tempFile;

    try (Connection connection = getConnection(jdbcUrl)) {
      Importer.importData(this.jsonFile, connection);
    }

    Files.copy(tempFile, sqliteFile, StandardCopyOption.REPLACE_EXISTING);

    return 0;
  }
}
