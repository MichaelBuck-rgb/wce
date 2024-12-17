package com.powergem.wce.commands;

import com.powergem.worstcasetrlim.model.WcResult;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import static com.powergem.worstcasetrlim.Utilities.getWorstCaseTrLim;

@CommandLine.Command(name = "scenarios")
public final class ListScenariosCommand implements Callable<Integer> {

  @CommandLine.Parameters(index = "0", description = "The JSON file to inspect.")
  private Path jsonFile;

  @Override
  public Integer call() {
    getWorstCaseTrLim(jsonFile).wcResults().stream().map(WcResult::title).forEach(System.out::println);

    return 0;
  }
}
