package com.powergem.wce.commands;

import com.powergem.worstcasetrlim.model.*;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import static com.powergem.worstcasetrlim.Utilities.getWorstCaseTrLim;

@CommandLine.Command(name = "unlocated")
public final class ListUnlocatedCommand implements Callable<Integer> {
  private static final Predicate<ILocation> FILTER = (location -> location.lat() == 0 && location.lon() == 0);

  @CommandLine.Parameters(index = "0", description = "The JSON file to inspect.")
  private Path jsonFile;

  @Override
  public Integer call() {
    System.out.printf("Listing nnlocated items in %s%n%n", this.jsonFile);

    List<WcResult> wcResults = getWorstCaseTrLim(jsonFile).wcResults();

    for (WcResult wcResult : wcResults) {
      System.out.printf("Unlocated items for scenario ID %s%n%n", wcResult.id());

      {
        List<Bus> unlocatedBuses = wcResult.buses().stream().filter(FILTER).toList();
        System.out.printf("  Found %d unlocated buses:%n", unlocatedBuses.size());
        unlocatedBuses.forEach(bus -> System.out.printf("    %s%n", bus));
      }

      {
        List<BranchTerminal> unlocatedBranchTerminals = wcResult.branchTerminalList().stream().filter(FILTER).toList();
        System.out.printf("%n  Found %d unlocated branch terminals:%n", unlocatedBranchTerminals.size());
        unlocatedBranchTerminals.forEach(branchTerminal -> System.out.printf("    %s%n", branchTerminal));
      }

      {
        List<StressGen> unlocatedStressGens = wcResult.StressGens().stream().filter(FILTER).toList();
        System.out.printf("%n  Found %d stress gens:%n", unlocatedStressGens.size());
        unlocatedStressGens.forEach(stressGen -> System.out.printf("    %s%n", stressGen));
      }

    }

    return 0;
  }

}
