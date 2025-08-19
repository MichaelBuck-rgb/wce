package com.powergem.wce.commands;

import com.powergem.TableBuilder;
import com.powergem.worstcasetrlim.Utilities;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "scenarios")
public final class ListScenariosCommand implements Callable<Integer> {
  @CommandLine.Option(names = {"-i", "--input"}, description = "The JSON file to inspect.", defaultValue = "WClusterTrLimSumJson.json")
  private Path jsonFile = Path.of("WClusterTrLimSumJson.json");

  @CommandLine.Option(names = {"-na", "--no-ansi"}, description = "Do not use ANSI codes in the output")
  private boolean noAnsi = false;

  @Override
  public Integer call() throws Exception {
    System.setProperty("wce.useAnsi", String.valueOf(!noAnsi));

    TableBuilder tableBuilder = new TableBuilder("id", "title", "version", "type");
    Utilities.getWorstCaseTrLim(this.jsonFile).wcResults().forEach(x -> tableBuilder.addRow(x.id(), x.title(), x.version(), x.type()));
    tableBuilder.printTable();

    return 0;
  }
}
