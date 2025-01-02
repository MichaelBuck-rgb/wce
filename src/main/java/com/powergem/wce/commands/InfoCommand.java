package com.powergem.wce.commands;

import com.powergem.worstcasetrlim.Utilities;
import com.powergem.worstcasetrlim.model.WorstCaseTrLim;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "info")
public final class InfoCommand implements Callable<Integer> {
  @CommandLine.Parameters(index = "0", description = "The JSON file to inspect.")
  private Path jsonFile;

  @Override
  public Integer call() {
    System.out.printf("%nInformation about %s%n%n", jsonFile);

    WorstCaseTrLim worstCaseTrLim = Utilities.getWorstCaseTrLim(this.jsonFile);
    worstCaseTrLim.wcResults().forEach(wcResult -> {
      System.out.printf("  Title                        : `%s'%n", wcResult.title());
      System.out.printf("  Version                      : `%s`%n", wcResult.version());
      System.out.printf("  ID                           : `%s`%n", wcResult.id());
      System.out.printf("  Number of buses              : %d%n", wcResult.buses().size());
      System.out.printf("  Number of `stressgens`       : %d%n", wcResult.StressGens().size());
      System.out.printf("  Number of `flowgates`        : %d%n", wcResult.flowgates().size());
      System.out.printf("  Number of `branch terminals` : %d%n", wcResult.branchTerminalList().size());
      System.out.println();
    });

    return 0;
  }
}
