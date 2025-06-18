package com.powergem.wce.commands;

import com.powergem.sql.UncheckedConnection;
import com.powergem.wce.DataFile;
import com.powergem.wce.Importer;
import com.powergem.wce.Utilities;
import com.powergem.wce.entities.BusEntity;
import picocli.CommandLine;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.Callable;

import static com.powergem.wce.Utilities.dumpFlowgate;
import static com.powergem.wce.Utils.getConnection;

@CommandLine.Command(name = "bus")
public final class ListBusCommand implements Callable<Integer> {
  @CommandLine.Parameters(index = "0", description = "The number of the bus to get the children of.")
  private int busNumber;

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
      Optional<BusEntity> optionalBus = dataFile.getBus(this.busNumber, scenarioId);
      if (optionalBus.isPresent()) {
        optionalBus.ifPresent(bus -> {
          System.out.println("[bus] [" + toString(bus) + "]");
          dataFile.getFlowgates(scenarioId, busNumber).forEach(flowgate -> dumpFlowgate(flowgate, dataFile, scenarioId, 2));
        });
      } else {
        System.out.println("Bus not found.");
      }
    }

    return 0;
  }

  private static String toString(BusEntity bus) {
    Map<String, String> map = new LinkedHashMap<>();

    map.put("id", String.valueOf(bus.id()));
    map.put("busnum", String.valueOf(bus.busnum()));
    map.put("busname", bus.busname());
    map.put("busvolt", String.format("%.2f", bus.busvolt()));
    map.put("busarea", bus.busarea());
    map.put("trlim", String.valueOf(bus.trlim()));
    map.put("location", Utilities.toString(bus.lat(), bus.lon()));

    return Utilities.toString(map);
  }
}
