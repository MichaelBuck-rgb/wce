package com.powergem.wce.commands;

import com.powergem.MonType;
import com.powergem.sql.UncheckedConnection;
import com.powergem.wce.DataFile;
import com.powergem.wce.Importer;
import com.powergem.wce.entities.BusEntity;
import com.powergem.wce.entities.ConstraintsEntity;
import com.powergem.wce.entities.FlowgateEntity;
import com.powergem.wce.entities.HarmerEntity;
import picocli.CommandLine;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import static com.powergem.wce.Utils.getConnection;

@CommandLine.Command(name = "children")
public final class ListChildrenCommand implements Callable<Integer> {
  @CommandLine.Parameters(index = "0", description = "The JSON file to inspect.")
  private Path jsonFile;

  @CommandLine.Parameters(index = "1", description = "The ID of the bus to get the constraints of.")
  private int busid;

  @Override
  public Integer call() throws Exception {
    String jdbcUrl = "jdbc:sqlite::memory:";

    int scenarioId = 1;


    try (Connection connection = getConnection(jdbcUrl)) {
      Importer.importData(this.jsonFile, connection);

      DataFile dataFile = new DataFile(new UncheckedConnection(connection));
      Optional<BusEntity> optionalBus = dataFile.getBus(this.busid);
      if (optionalBus.isPresent()) {
        optionalBus.ifPresent(bus -> {
          System.out.printf("[Bus]: ID: %d, Number: %d, Name: %s, Voltage: %f, Area: %s, TrLim: %f, Location: (%f, %f)%n", bus.id(), bus.busnum(), bus.busname(), bus.busvolt(), bus.busarea(), bus.trlime(), bus.lat(), bus.lon());
          List<FlowgateEntity> flowgates = dataFile.getFlowgates(scenarioId, busid);
          flowgates.forEach(flowgate -> {
            System.out.printf("  [Flowgate] id: %d, dfax: %f, trlim: %f, mon: '%s', con: '%s'%n", flowgate.id(), flowgate.dfax(), flowgate.trlim(), flowgate.mon(), flowgate.con());

            List<ConstraintsEntity> constraints = dataFile.getConstraints(scenarioId, flowgate.id());
            constraints.forEach(constraint -> {
              String strMonType = MonType.getMonType(constraint.monType())
                      .map(monType -> switch (monType) {
                        case LINE -> "Line";
                        case TRANSFORMER -> "Transformer";
                        default -> "Unknown";
                      })
                      .orElse("Unknown");
              System.out.printf("    [Constraint] monType: %d (%s), frBus: %d, toBus: %d%n", constraint.monType(), strMonType, constraint.frBus(), constraint.toBus());
            });

            List<HarmerEntity> harmers = dataFile.getHarmers(scenarioId, flowgate.id());
            harmers.forEach(harmer -> {
              System.out.printf("    [Harmer]: id: %d, dfax: %f, MW Change: %f, MW Impact: %f, pMax: %f, pGen: %f%n", harmer.id(), harmer.dfax(), harmer.mwchange(), harmer.mwimpact(), harmer.pmax(), harmer.pgen());
            });
          });
        });
      } else {
        System.out.println("Bus not found.");
      }
    }

    return 0;
  }
}
