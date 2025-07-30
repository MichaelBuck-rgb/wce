package com.powergem.wce.commands;

import com.powergem.worstcasetrlim.Utilities;
import com.powergem.worstcasetrlim.model.WorstCaseTrLim;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "info")
public final class InfoCommand implements Callable<Integer> {
  @CommandLine.Option(names = {"-i", "--input"}, description = "The JSON file to inspect.", defaultValue = "WClusterTrLimSumJson.json")
  private Path jsonFile = Path.of("WClusterTrLimSumJson.json");

  @Override
  public Integer call() {
    System.out.printf("%nInformation about %s%n%n", jsonFile);

    WorstCaseTrLim worstCaseTrLim = Utilities.getWorstCaseTrLim(this.jsonFile);
    worstCaseTrLim.wcResults().forEach(wcResult -> {
      System.out.printf("  Title                   : `%s'%n", wcResult.title());
      System.out.printf("  Version                 : `%s`%n", wcResult.version());
      System.out.printf("  ID                      : `%s`%n", wcResult.id());
      System.out.printf("  Type                    : `%s`%n", wcResult.type());
      System.out.printf("  Buses                   : %d%n", wcResult.buses().size());
      System.out.printf("  stressgens              : %d%n", wcResult.StressGens().size());
      System.out.printf("  flowgates               : %d%n", wcResult.flowgates().size());
      System.out.printf("  branch terminals        : %d%n", wcResult.branchTerminalList() == null ? null : wcResult.branchTerminalList().size());
      System.out.printf("  line cost data          : %d%n", wcResult.lineCostData() == null ? null : wcResult.lineCostData().size());
      System.out.printf("  transformer cost data   : %d%n", wcResult.transformerCostData() == null ? null : wcResult.transformerCostData().size());
      System.out.println();
    });

    return 0;
  }
}
