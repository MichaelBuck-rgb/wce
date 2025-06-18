package com.powergem.wce.commands;

import com.powergem.sql.UncheckedConnection;
import com.powergem.wce.DataFile;
import com.powergem.wce.Importer;
import com.powergem.wce.Utilities;
import com.powergem.wce.entities.BusEntity;
import com.powergem.wce.entities.FlowgateEntity;
import picocli.CommandLine;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import static com.powergem.wce.Utilities.dumpFlowgate;
import static com.powergem.wce.Utils.getConnection;

@CommandLine.Command(name = "children")
public final class ListChildrenCommand implements Callable<Integer> {
  @CommandLine.Parameters(index = "0", description = "The JSON file to inspect.", defaultValue = "WClusterTrLimSumJson.json")
  private Path jsonFile;

  @CommandLine.Parameters(index = "1", description = "The number of the bus to get the children of.")
  private int busNumber;

  @Override
  public Integer call() throws Exception {
    String jdbcUrl = "jdbc:sqlite::memory:";

    int scenarioId = 1;


    try (Connection connection = getConnection(jdbcUrl)) {
      Importer.importData(this.jsonFile, connection);

      DataFile dataFile = new DataFile(new UncheckedConnection(connection));
      Optional<BusEntity> optionalBus = dataFile.getBus(this.busNumber);
      if (optionalBus.isPresent()) {
        optionalBus.ifPresent(bus -> {
          String location = Utilities.toString(bus.lat(), bus.lon());
          System.out.printf("[Bus] [ID: %d, Number: %d, Name: %s, Voltage: %.2f, Area: %s, TrLim: %f, Location: %s]%n", bus.id(), bus.busnum(), bus.busname(), bus.busvolt(), bus.busarea(), bus.trlime(), location);
          List<FlowgateEntity> flowgates = dataFile.getFlowgates(scenarioId, busNumber);
          flowgates.forEach(flowgate -> dumpFlowgate(flowgate, dataFile, scenarioId, 2));
        });
      } else {
        System.out.println("Bus not found.");
      }
    }

    return 0;
  }
}
