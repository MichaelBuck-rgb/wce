package com.powergem.wce.commands;

import com.powergem.wce.ReportType;
import com.powergem.wce.Utilities;
import com.powergem.worstcasetrlim.model.Bus;
import com.powergem.worstcasetrlim.model.Flowgate;
import com.powergem.worstcasetrlim.model.WcResult;
import com.powergem.worstcasetrlim.model.WorstCaseTrLim;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "bus")
public final class ListBusCommand implements Callable<Integer> {
  @CommandLine.Parameters(index = "0", description = "The number of the bus to get the children of.")
  private int busNumber;

  @CommandLine.Option(names = {"-i", "--input"}, description = "The JSON file to inspect.", defaultValue = "WClusterTrLimSumJson.json")
  private Path jsonFile = Path.of("WClusterTrLimSumJson.json");

  @CommandLine.Option(names = {"-s", "--scenario"}, description = "The ID of the scenario to get the bus from.", defaultValue = "1")
  private int scenarioId = 1;

  @CommandLine.Option(names = {"-na", "--no-ansi"}, description = "Do not use ANSI codes in the output")
  private boolean noAnsi = false;

  @CommandLine.Option(names = {"-X", "--exclude"}, description = "Objects to exclude from the list.")
  private String exclude;

  @Override
  public Integer call() throws Exception {
    System.setProperty("wce.useAnsi", String.valueOf(!noAnsi));

    Set<ReportType> exclusions = this.exclude == null ? Collections.emptySet() : Utilities.toExclusions(this.exclude);

    WorstCaseTrLim worstCaseTrLim = com.powergem.worstcasetrlim.Utilities.getWorstCaseTrLim(this.jsonFile);
    WcResult scenario = worstCaseTrLim.wcResults().stream()
            .filter(wcResult -> Integer.parseInt(wcResult.id()) == this.scenarioId)
            .findAny()
            .orElseThrow();

    Bus bus = scenario.buses().stream()
            .filter(aBus -> aBus.busnum() == this.busNumber)
            .findAny()
            .orElseThrow();

    List<Flowgate> flowgates = scenario.flowgates();

    Utilities.dumpBus(bus, flowgates, exclusions, 0);

    return 0;
  }

}
