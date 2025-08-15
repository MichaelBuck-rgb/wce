package com.powergem.wce.commands;

import com.powergem.worstcasetrlim.Utilities;
import com.powergem.worstcasetrlim.model.Flowgate;
import com.powergem.worstcasetrlim.model.WcResult;
import com.powergem.worstcasetrlim.model.WorstCaseTrLim;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

import static com.powergem.wce.Importer.decrypt;

@CommandLine.Command(
        name = "reindex",
        description = "Re-indexes the 'id' attribute of wcResult and each flowgate.",
        usageHelpWidth = 132
)
public final class ReindexCommand implements Callable<Integer> {
  @CommandLine.Option(names = {"-i", "--input"}, description = "The JSON file to inspect.", defaultValue = "WClusterTrLimSumJson.json")
  private Path jsonFile = Path.of("WClusterTrLimSumJson.json");

  @CommandLine.Option(names = {"-n", "--no-ansi"}, description = "Do not use ANSI codes in the output")
  private boolean noAnsi = false;

  @Override
  public Integer call() throws Exception {
    System.setProperty("wce.useAnsi", String.valueOf(!noAnsi));

    this.jsonFile = this.jsonFile.normalize().toAbsolutePath();

    WorstCaseTrLim worstCaseTrLim = Utilities.getWorstCaseTrLim(this.jsonFile);

    worstCaseTrLim = decrypt(worstCaseTrLim);

    final class ScenarioIndex implements UnaryOperator<WcResult> {
      private int index = 0;

      @Override
      public WcResult apply(WcResult wcResult) {
        return wcResult.withId(String.valueOf(index++));
      }
    }

    List<WcResult> wcResults = worstCaseTrLim.wcResults().stream()
            .map(new ScenarioIndex())
            .map(wcResult -> {
              AtomicInteger index = new AtomicInteger(0);
              List<Flowgate> reindexedFlowgsates = wcResult.flowgates().stream()
                      .map(flowgate -> flowgate.withId(index.getAndIncrement()))
                      .toList();
              return wcResult.withFlowgates(reindexedFlowgsates);
            })
            .toList();

    worstCaseTrLim = new WorstCaseTrLim(wcResults);

    Jsonb jsonb = Jsonb.builder().build();
    JsonType<WorstCaseTrLim> worstCaseTrLimJsonType = jsonb.type(WorstCaseTrLim.class);

//    try (OutputStream inputStream = Files.newOutputStream(Paths.get("reindex.json"))) {
//      worstCaseTrLimJsonType.toJson(worstCaseTrLim, inputStream);
//    } catch (IOException e) {
//      throw new UncheckedIOException(e);
//    }
    worstCaseTrLimJsonType.toJson(worstCaseTrLim, System.out);

    return 0;
  }
}
