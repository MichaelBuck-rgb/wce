package com.powergem.wce.commands;

import com.powergem.wce.Importer;
import com.powergem.wce.Utils;
import com.powergem.worstcasetrlim.model.Bus;
import com.powergem.worstcasetrlim.model.Flowgate;
import picocli.CommandLine;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;

import static com.powergem.wce.Utils.getConnection;

@CommandLine.Command(name = "flowgates")
public final class ListFlowgatesCommand implements Callable<Integer> {
  @CommandLine.Parameters(index = "1", description = "The ID of the bus to get the flowgates of.")
  private int busid;

  @CommandLine.Parameters(index = "0", description = "The JSON file to inspect.")
  private Path jsonFile;

  @Override
  public Integer call() throws Exception {
    String jdbcUrl = "jdbc:sqlite::memory:";

    try (Connection connection = getConnection(jdbcUrl)) {
      Importer.importData(this.jsonFile, connection);

      final Bus bus;
      try (PreparedStatement statement = connection.prepareStatement("select * from buses where id = ?")) {
        statement.setInt(1, busid);
        try (ResultSet resultSet = statement.executeQuery()) {
          if (resultSet.next()) {
            bus = toBus(resultSet);
          } else {
            System.err.println("Bus not found for specified id: " + busid);
            return -1;
          }
        }
      }

      List<Flowgate> flowgates = new ArrayList<>();
      try (PreparedStatement statement = connection.prepareStatement("select * from flowgates where busid = ?")) {
        statement.setInt(1, busid);
        try (ResultSet rs = statement.executeQuery()) {
          while (rs.next()) {
            flowgates.add(Utils.toFlowgate(rs));
          }
        }
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }

      System.out.println("Flowgates for bus ID: " + busid);
      System.out.println("  " + bus);
      System.out.println();
      flowgates.forEach(System.out::println);
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
