package com.powergem.wce.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powergem.wce.Importer;
import com.powergem.wce.TransmissionLine;
import com.powergem.worstcasetrlim.model.BranchTerminal;
import com.powergem.worstcasetrlim.model.Bus;
import picocli.CommandLine;

import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static com.powergem.wce.Utils.getConnection;

@CommandLine.Command(name = "constraints")
public class ListConstraintsCommand implements Callable<Integer> {
  @CommandLine.Parameters(index = "1", description = "The ID of the bus to get the constraints of.")
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

      Function<Integer, BranchTerminal> getter = branchTerminalID -> {
        try (PreparedStatement statement = connection.prepareStatement("select * from branchterminals where id = ?")) {
          statement.setInt(1, branchTerminalID);
          try (ResultSet rs = statement.executeQuery()) {
            return rs.next() ? from(rs) : null;
          }
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      };

      Map<Integer, BranchTerminal> terminalCache = new HashMap<>();
      Set<TransmissionLine> transmissionLines = new LinkedHashSet<>();

      try (PreparedStatement statement = connection.prepareStatement("select id, frBuses, toBuses, monType from flowgates where busid = ?")) {
        statement.setInt(1, busid);
        try (ResultSet rs = statement.executeQuery()) {
          while (rs.next()) {
            int[] monTypes = toIntArray(rs.getString("monType"));
            int[] frBuses = toIntArray(rs.getString("frBuses"));
            int[] toBuses = toIntArray(rs.getString("toBuses"));
            for (int i = 0; i < monTypes.length; i++) {
              if (isTransmissionLine(monTypes[i])) {
                BranchTerminal from = terminalCache.computeIfAbsent(frBuses[i], getter);
                BranchTerminal to = terminalCache.computeIfAbsent(toBuses[i], getter);
                transmissionLines.add(new TransmissionLine(from, to));
              }
            }
          }
        }
      }

      System.out.println("Limiting constraints for bus ID: " + busid);
      System.out.println("  " + bus);
      System.out.println();
      transmissionLines.forEach(transmissionLine -> {
        System.out.println("  Branch terminals:");
        System.out.println("    " + transmissionLine.from());
        System.out.println("    " + transmissionLine.to());
        System.out.println();
      });
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

  private static int[] toIntArray(String strJson) throws JsonProcessingException {
  ObjectMapper objectMapper = new ObjectMapper();
  return objectMapper.readValue(strJson, int[].class);
}

private static boolean isTransmissionLine(int monType) {
  return monType == 1;
}

private static BranchTerminal from(ResultSet rs) throws SQLException {
  int id = rs.getInt("id");
  String name = rs.getString("name");
  double kv = rs.getDouble("kv");
  int areaNum = rs.getInt("areaNum");
  String areaName = rs.getString("areaName");
  double lat = rs.getDouble("lat");
  double lon = rs.getDouble("lon");

  return new BranchTerminal(id, name, kv, areaNum, areaName, lat, lon);
}
}
