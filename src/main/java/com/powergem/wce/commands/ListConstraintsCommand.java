package com.powergem.wce.commands;

import com.powergem.wce.Importer;
import com.powergem.wce.TransmissionLine;
import com.powergem.worstcasetrlim.model.BranchTerminal;
import com.powergem.worstcasetrlim.model.Bus;
import com.powergem.worstcasetrlim.model.Flowgate;
import picocli.CommandLine;

import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Callable;

import static com.powergem.wce.Utils.getConnection;
import static com.powergem.wce.Utils.toFlowgate;

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

      // get all flowgates of the bus
      List<Flowgate> flowgates = new ArrayList<>();
      try (PreparedStatement statement = connection.prepareStatement("select * from flowgates where busid = ?")) {
        statement.setInt(1, busid);
        try (ResultSet resultSet = statement.executeQuery()) {
          while (resultSet.next()) {
            Flowgate flowgate = toFlowgate(resultSet);
            flowgates.add(flowgate);
          }
        }
      }

      // get the branch constraints of each bus
      Map<Integer, Thing> fromThings = HashMap.newHashMap(flowgates.size());
      Map<Integer, Thing> toThings = HashMap.newHashMap(flowgates.size());
      try (PreparedStatement statement = connection.prepareStatement("select * from branchterminals bt inner join (select rowid as rid, flowgateid, frbus, tobus from constraints where flowgateid = ? and montype = 1) c on bt.id in (c.frbus, c.tobus) order by rid;")) {
        for (Flowgate flowgate : flowgates) {
          statement.setInt(1, flowgate.id());
          try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
              Thing thing = Thing.toThing(resultSet, flowgate);
              if (thing.isFrom()) {
                fromThings.put(thing.rid(), thing);
              } else {
                toThings.put(thing.rid(), thing);
              }
            }
          }
        }
      }

      Map<Flowgate, TransmissionLine> transmissionLines = HashMap.newHashMap(flowgates.size());
      for (Map.Entry<Integer, Thing> fromEntry : fromThings.entrySet()) {
        Thing fromThing = fromEntry.getValue();
        BranchTerminal fromBranchTerminal = toBranchTerminal(fromThing);
        BranchTerminal toBranchTerminal = toBranchTerminal(toThings.get(fromThing.rid));
        TransmissionLine transmissionLine = new TransmissionLine(fromBranchTerminal, toBranchTerminal);
        transmissionLines.put(fromThing.flowgate, transmissionLine);
      }

      System.out.println("Limiting constraints for bus ID: " + busid);
      System.out.println("  " + bus);
      System.out.println();
      transmissionLines.forEach((flowgate, transmissionLine) -> {
        System.out.printf("  Branch terminals for %s%n", flowgate);
        System.out.println("    " + transmissionLine.from());
        System.out.println("    " + transmissionLine.to());
        System.out.println();
      });
    }

    return 0;
  }

  private BranchTerminal toBranchTerminal(Thing thing) {
    return new BranchTerminal(thing.id, thing.name, thing.kv, thing.areanum, thing.areaname, thing.lat, thing.lon);
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

  private record Thing(Flowgate flowgate, int rid, int id, String name, double kv, int areanum, String areaname, int frbus, int tobus,
                       double lat, double lon) {

    public boolean isFrom() {
      return id == frbus;
    }

    private static Thing toThing(ResultSet rs, Flowgate flowgate) throws SQLException {
      return new Thing(
              flowgate,
              rs.getInt("rid"),
              rs.getInt("id"),
              rs.getString("name"),
              rs.getDouble("kv"),
              rs.getInt("areanum"),
              rs.getString("areaname"),
              rs.getInt("frbus"),
              rs.getInt("tobus"),
              rs.getDouble("lat"),
              rs.getDouble("lon")
      );
    }
  }
}
