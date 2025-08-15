package com.powergem.wce.commands;

import com.powergem.worstcasetrlim.Utilities;
import com.powergem.worstcasetrlim.model.*;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@CommandLine.Command(
        name = "extract",
        description = "Extracts the specified buses to the screen.",
        usageHelpWidth = 132
)
public final class ExtractCommand implements Callable<Integer> {
  @CommandLine.Parameters(index = "0..", description = "List of Bus numbers.")
  private List<Integer> busNumbers;

  @CommandLine.Option(names = {"-i", "--input"}, description = "The JSON file to inspect.", defaultValue = "WClusterTrLimSumJson.json")
  private Path jsonFile = Path.of("WClusterTrLimSumJson.json");

  @CommandLine.Option(names = {"-na", "--no-ansi"}, description = "Do not use ANSI codes in the output")
  private boolean noAnsi = false;

  @Override
  public Integer call() throws Exception {
    System.setProperty("wce.useAnsi", String.valueOf(!noAnsi));

    this.jsonFile = this.jsonFile.normalize().toAbsolutePath();

    WorstCaseTrLim worstCaseTrLim = Utilities.getWorstCaseTrLim(this.jsonFile);

    List<WcResult> newWcResults = worstCaseTrLim.wcResults().stream()
            .map(wcResult -> {
              List<Bus> newBuses = wcResult.buses().stream()
                      .filter(bus -> this.busNumbers.contains(bus.busnum()))
                      .toList();

              List<Flowgate> newFlowgates;
              {
                Set<Integer> referencedBuses = newBuses.stream()
                        .map(Bus::id)
                        .collect(Collectors.toSet());

                newFlowgates = wcResult.flowgates().stream()
                        .filter(flowgate -> referencedBuses.contains(flowgate.busid()))
                        .toList();
              }

              List<StressGen> newStressGens;
              {
                Set<Integer> referencedStressgens = newFlowgates.stream()
                        .flatMap(flowgate -> flowgate.harmers().stream())
                        .map(Harmer::index)
                        .collect(Collectors.toSet());

                newStressGens = wcResult.StressGens().stream()
                        .filter(stressGen -> referencedStressgens.contains(stressGen.id()))
                        .toList();
              }

              List<BranchTerminal> newBranchTerminals;
              if (wcResult.branchTerminalList() != null && !wcResult.branchTerminalList().isEmpty()) {
                Set<Integer> referencedBranchTerminals = newFlowgates.stream()
                        .filter(flowgate -> flowgate.frBuses() != null && flowgate.toBuses() != null && (flowgate.frBuses().length > 0 || flowgate.toBuses().length > 0))
                        .map(flowgate -> Map.entry(flowgate.frBuses(), flowgate.toBuses()))
                        .map(entry -> Map.entry(IntStream.of(entry.getKey()), IntStream.of(entry.getValue())))
                        .flatMapToInt(entry -> IntStream.concat(entry.getKey(), entry.getValue()))
                        .boxed()
                        .collect(Collectors.toSet());

                newBranchTerminals = wcResult.branchTerminalList().stream()
                        .filter(branchTerminal -> referencedBranchTerminals.contains(branchTerminal.id()))
                        .toList();
              } else {
                newBranchTerminals = List.of();
              }

              List<LineCostDatum> newLineCostData;
              List<TransformerCostData> newTransformerCostData;
              if ((wcResult.lineCostData() != null && !wcResult.lineCostData().isEmpty())) {
                Set<Integer> referencedEquipment = newFlowgates.stream()
                        .flatMapToInt(flowgate -> IntStream.of(flowgate.equipmentIndex()))
                        .boxed()
                        .collect(Collectors.toSet());

                newLineCostData = wcResult.lineCostData().stream()
                        .filter(lineCostDatum -> referencedEquipment.contains(lineCostDatum.id()))
                        .toList();

                newTransformerCostData = wcResult.transformerCostData().stream()
                        .filter(transformerCostDatum -> referencedEquipment.contains(transformerCostDatum.id()))
                        .toList();
              } else {
                newLineCostData = List.of();
                newTransformerCostData = List.of();
              }

              return wcResult
                      .withBuses(newBuses)
                      .withFlowgates(newFlowgates)
                      .withStressGens(newStressGens)
                      .withLineCostData(newLineCostData)
                      .withTransformerCostData(newTransformerCostData)
                      .withBranchTerminalList(newBranchTerminals);
            })
            .toList();

    worstCaseTrLim = new WorstCaseTrLim(newWcResults);

    Jsonb jsonb = Jsonb.builder().build();
    JsonType<WorstCaseTrLim> customerType = jsonb.type(WorstCaseTrLim.class);
    customerType.toJson(worstCaseTrLim, System.out);

    return 0;
  }
}
