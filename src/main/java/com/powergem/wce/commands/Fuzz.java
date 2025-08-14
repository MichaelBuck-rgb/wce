package com.powergem.wce.commands;

import com.powergem.sql.UncheckedConnection;
import com.powergem.wce.*;
import com.powergem.wce.entities.*;
import com.powergem.wce.mapstruct.*;
import com.powergem.worstcasetrlim.model.*;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import picocli.CommandLine;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static com.powergem.wce.Utils.getConnection;

@CommandLine.Command(
        name = "fuzz",
        description = "Creates dummy data.",
        usageHelpWidth = 132
)
public final class Fuzz implements Callable<Integer> {
  @CommandLine.Option(names = {"-i", "--input"}, description = "The JSON file to inspect.", defaultValue = "WClusterTrLimSumJson.json")
  private Path jsonFile = Path.of("WClusterTrLimSumJson.json");

  @CommandLine.Option(names = {"-f", "--force"}, description = "Overwrite existing file.")
  private boolean force = false;

  @CommandLine.Option(names = {"-n", "--no-ansi"}, description = "Do not use ANSI codes in the output")
  private boolean noAnsi = false;

  @Override
  public Integer call() throws Exception {
    System.setProperty("wce.useAnsi", String.valueOf(!noAnsi));

    this.jsonFile = this.jsonFile.normalize().toAbsolutePath();

    String jdbcUrl = "jdbc:sqlite::memory:";
//    Files.delete(Paths.get("fuzz.db"));
//    String jdbcUrl = "jdbc:sqlite:fuzz.db";

    List<WcResult> wcResults = new ArrayList<>();

    try (Connection connection = getConnection(jdbcUrl)) {
      Importer.importData(this.jsonFile, connection);
      try (UncheckedConnection uncheckedConnection = new UncheckedConnection(connection)) {
        try (ScenariosTable scenariosTable = new ScenariosTable(uncheckedConnection)) {
          scenariosTable.scenarios().forEach(scenarioTable -> {
            try (FlowgateTable flowgateTable = scenarioTable.getFlowgates()) {
              long numberOfFlowgates = flowgateTable.getCount();

              long numberOfEquipment = numberOfFlowgates / 10;

              try (LineCostDataTable lineCostDataTable = scenarioTable.getLineCostData()) {
                for (int id = 0; id < numberOfEquipment; ++id) {
                  float length = (float) (Math.random() * 100);
                  float maxRatingPerLine = (float) (Math.random() * 100);
                  float maxAllowedFlowPerLine = (float) (Math.random() * 100);
                  float upgradeCost = (float) (Math.random() * 100);
                  float newLineCost = (float) (Math.random() * 100);
                  LineCostDatumEntity lineCostDatumEntry = new LineCostDatumEntity(scenarioTable.getScenarioId(), id, length, maxRatingPerLine, maxAllowedFlowPerLine, upgradeCost, newLineCost);
                  lineCostDataTable.insert(lineCostDatumEntry);
                }
              }

              // update the flowgates to have at least one line-cost-data entry
              try (FlowgateTable.Updater updater = flowgateTable.updater(1000)) {
                int lineCostDataId = 0;
                for (FlowgateEntity flowgateEntity : flowgateTable) {
                  flowgateEntity = flowgateEntity.withEquipmentIndex(lineCostDataId);
                  updater.accept(flowgateEntity);
                  lineCostDataId++;
                }
              }
            } catch (RuntimeException e) {
              throw e;
            } catch (Exception e) {
              throw new RuntimeException(e);
            }

            WcResult wcResult = toWcResult(scenarioTable);
            wcResults.add(wcResult);
          });
        }
      }
    }

    WorstCaseTrLim worstCaseTrLim = new WorstCaseTrLim(wcResults);

    Jsonb jsonb = Jsonb.builder().build();
    JsonType<WorstCaseTrLim> worstCaseTrLimJsonType = jsonb.type(WorstCaseTrLim.class);

//    try (OutputStream inputStream = Files.newOutputStream(Paths.get("fuzz.json"))) {
//      worstCaseTrLimJsonType.toJson(worstCaseTrLim, inputStream);
//    } catch (IOException e) {
//      throw new UncheckedIOException(e);
//    }
      worstCaseTrLimJsonType.toJson(worstCaseTrLim, System.out);

    return 0;
  }

  private static WcResult toWcResult(ScenarioTable scenarioTable) {
    List<Bus> buses = scenarioTable.getBuses().toList().stream().map(BusMapper.INSTANCE::toBus).toList();
    List<StressGen> stressGens = scenarioTable.getStressgens().toList().stream().map(StressGenMapper.INSTANCE::toStressGen).toList();
    List<Tuple> flowgateTuples = new ArrayList<>();
    scenarioTable.getFlowgates().forEach(flowgateEntity -> {
      List<HarmerEntity> harmerEntities = scenarioTable.getHarmers(flowgateEntity.id());
      ScenarioTable.ConstraintsInfo constraintInfo = scenarioTable.getConstraintInfoForFlowgate(flowgateEntity.id());
      int[] equipmentIndex = flowgateEntity.equipment_index().stream().mapToInt(Integer::intValue).toArray();
      Tuple tuple = new Tuple(flowgateEntity, harmerEntities, constraintInfo.frBus(), constraintInfo.toBus(), constraintInfo.monType(), equipmentIndex);
      flowgateTuples.add(tuple);
    });

    List<Flowgate> flowgates = flowgateTuples.stream().map(FlowgateMapper.INSTANCE::toFlowgate).toList();

    List<LineCostData> lineCostData = scenarioTable.getLineCostData().asList().stream()
            .map(LineCostDatumEntityMapper.INSTANCE::toFlowgate)
            .toList();

    return new WcResult("3.0", String.valueOf(scenarioTable.getScenarioId()), scenarioTable.getName(), buses, stressGens, flowgates, List.of(), scenarioTable.getMode(), lineCostData, List.of());
  }
}
