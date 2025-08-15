package com.powergem.wce.commands;

import com.powergem.wce.*;
import com.powergem.wce.entities.*;
import com.powergem.wce.mapstruct.*;
import com.powergem.worstcasetrlim.Utilities;
import com.powergem.worstcasetrlim.model.*;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

@CommandLine.Command(
        name = "fuzz",
        description = "Creates dummy data.",
        usageHelpWidth = 132
)
public final class FuzzCommand implements Callable<Integer> {
  @CommandLine.Option(names = {"-i", "--input"}, description = "The JSON file to inspect.", defaultValue = "WClusterTrLimSumJson.json")
  private Path jsonFile = Path.of("WClusterTrLimSumJson.json");

  @CommandLine.Option(names = {"-n", "--no-ansi"}, description = "Do not use ANSI codes in the output")
  private boolean noAnsi = false;

  @Override
  public Integer call() throws Exception {
    // todo: should probably check the version and upgrade if needed
    System.setProperty("wce.useAnsi", String.valueOf(!noAnsi));

    this.jsonFile = this.jsonFile.normalize().toAbsolutePath();

    WorstCaseTrLim worstCaseTrLim = Utilities.getWorstCaseTrLim(this.jsonFile);

    List<WcResult> wcResults = worstCaseTrLim.wcResults().stream()
            .map(wcResult -> {
              wcResult = wcResult.withVersion("3.0");

              List<LineCostDatum> lineCostData = IntStream.range(0, wcResult.flowgates().size() / 10)
                      .mapToObj(id -> {
                        float length = (float) (Math.random() * 100);
                        float maxRatingPerLine = (float) (Math.random() * 100);
                        float maxAllowedFlowPerLine = (float) (Math.random() * 100);
                        float upgradeCost = (float) (Math.random() * 100);
                        float newLineCost = (float) (Math.random() * 100);
                        return new LineCostDatum(id, length, maxRatingPerLine, maxAllowedFlowPerLine, upgradeCost, newLineCost);
                      })
                      .toList();

              wcResult = wcResult.withLineCostData(lineCostData);

              List<Flowgate> newFlowgates = wcResult.flowgates().stream()
                      .map(flowgate -> {
                        int equipmentIndex = (int) (Math.random() * lineCostData.size());
                        return flowgate.withEquipmentIndex(new int[]{equipmentIndex});
                      })
                      .toList();

              return wcResult.withFlowgates(newFlowgates);
            })
            .toList();

    worstCaseTrLim = new WorstCaseTrLim(wcResults);

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
}
