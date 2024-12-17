package com.powergem.wce.commands;

import com.powergem.wce.Importer;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import static com.powergem.wce.Utils.getConnection;

@CommandLine.Command(
        name = "sqlite",
        description = "Converts the specified WClusterTrLimSumJson.json into constituent tables in a SQLite database.",
        usageHelpWidth = 132
)
public class SqliteCommand implements Callable<Integer> {

  @CommandLine.Parameters(index = "0", description = "The WClusterTrLimSumJson.json to import")
  private Path file;

  @Override
  public Integer call() {
    if (Files.notExists(this.file)) {
      System.err.println("File '" + this.file + "' does not exist.");
      return 1;
    }

    try (Connection connection = getConnection("jdbc:sqlite:worstcasetrlim.sqlite")) {
      Importer.importData(this.file, connection);
    } catch (SQLException e) {
      e.printStackTrace(System.err);
      return 1;
    }

    return 0;
  }
}
