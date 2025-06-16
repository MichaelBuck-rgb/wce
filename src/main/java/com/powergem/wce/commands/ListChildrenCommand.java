package com.powergem.wce.commands;

import com.powergem.MonType;
import com.powergem.sql.UncheckedConnection;
import com.powergem.wce.DataFile;
import com.powergem.wce.Importer;
import com.powergem.wce.entities.BusEntity;
import com.powergem.wce.entities.ConstraintsEntity;
import com.powergem.wce.entities.FlowgateEntity;
import com.powergem.wce.entities.HarmerEntity;
import com.powergem.worstcasetrlim.model.BranchTerminal;
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
          System.out.printf("[Bus] ID: %d, Number: %d, Name: %s, Voltage: %.2f, Area: %s, TrLim: %f, Location: (%f, %f)%n", bus.id(), bus.busnum(), bus.busname(), bus.busvolt(), bus.busarea(), bus.trlime(), bus.lat(), bus.lon());
          List<FlowgateEntity> flowgates = dataFile.getFlowgates(scenarioId, busid);
          flowgates.forEach(flowgate -> {
            System.out.printf("  [Flowgate] id: %d, dfax: %f, trlim: %f, mon: '%s', con: '%s'%n", flowgate.id(), flowgate.dfax(), flowgate.trlim(), flowgate.mon(), flowgate.con());

            List<ConstraintsEntity> constraints = dataFile.getConstraints(scenarioId, flowgate.id());
            constraints.forEach(constraint -> {
              MonType.getMonType(constraint.monType()).ifPresent(monType -> {
                if (monType == MonType.LINE) {
                  BranchTerminal from = dataFile.getBranchBus(scenarioId, constraint.frBus()).orElseThrow();
                  BranchTerminal to = dataFile.getBranchBus(scenarioId, constraint.toBus()).orElseThrow();
                  System.out.println("    [Line Constraint]");
                  System.out.printf("      [%s]%n", toString(from));
                  System.out.printf("      [%s]%n", toString(to));
                } else if (monType == MonType.TRANSFORMER) {
                  BranchTerminal from = dataFile.getBranchBus(scenarioId, constraint.frBus()).orElseThrow();
                  System.out.println("    [Transformer Constraint]");
                  System.out.printf("      [%s]%n", toString(from));
                } else {
                  System.out.println("    [Unknown Constraint]");
                }
              });
            });

            List<HarmerEntity> harmers = dataFile.getHarmers(scenarioId, flowgate.id());
            harmers.forEach(harmer -> System.out.printf("    [Harmer]: id: %d, dfax: %f, MW Change: %f, MW Impact: %f, pMax: %f, pGen: %f%n", harmer.id(), harmer.dfax(), harmer.mwchange(), harmer.mwimpact(), harmer.pmax(), harmer.pgen()));
          });
        });
      } else {
        System.out.println("Bus not found.");
      }
    }

    return 0;
  }


  private static String toString(BranchTerminal branchTerminal) {
    return "id: %d, name: '%s', kv: %.2f, areanum: %d, areaname: '%s', (%f, %f)".formatted(branchTerminal.id(), branchTerminal.name(), branchTerminal.kv(), branchTerminal.areanum(), branchTerminal.areaname(), branchTerminal.lat(), branchTerminal.lon());
  }
}
