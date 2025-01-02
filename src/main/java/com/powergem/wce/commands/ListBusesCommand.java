package com.powergem.wce.commands;

import com.powergem.wce.Importer;
import com.powergem.worstcasetrlim.model.Bus;
import picocli.CommandLine;

import java.nio.file.Path;
import java.sql.*;
import java.util.concurrent.Callable;

import static com.powergem.wce.Utils.getConnection;

@CommandLine.Command(name = "buses")
public class ListBusesCommand implements Callable<Integer> {
  @CommandLine.Parameters(index = "0", description = "The JSON file to inspect.")
  private Path jsonFile;

  @Override
  public Integer call() throws Exception {
    String jdbcUrl = "jdbc:sqlite::memory:";

    try (Connection connection = getConnection(jdbcUrl)) {
      Importer.importData(this.jsonFile, connection);

      try (Statement statement = connection.createStatement()) {
        try (ResultSet resultSet = statement.executeQuery("select * from buses")) {
          while (resultSet.next()) {
            Bus bus = toBus(resultSet);
            System.out.println(bus);
          }
        }
      }
    }

    return 0;
  }

  private static Bus toBus(ResultSet resultSet) throws SQLException {
    return new Bus(
            resultSet.getInt("id"),
            resultSet.getInt("busnum"),
            resultSet.getString("busname"),
            resultSet.getDouble("busvolt"),
            resultSet.getString("busarea"),
            resultSet.getDouble("trlim"),
            resultSet.getDouble("lat"),
            resultSet.getDouble("lon")
    );
  }
}
