package com.powergem.worstcasetrlim;

import com.powergem.worstcasetrlim.model.WcResult;
import com.powergem.worstcasetrlim.model.WorstCaseTrLim;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class Utilities {

  private Utilities() {
  }

  public static WorstCaseTrLim getWorstCaseTrLim(Path file) {
    Jsonb jsonb = Jsonb.builder().build();
    JsonType<WorstCaseTrLim> customerType = jsonb.type(WorstCaseTrLim.class);

    WorstCaseTrLim worstCaseTrLim;
    try (InputStream inputStream = Files.newInputStream(file)) {
      worstCaseTrLim = customerType.fromJson(inputStream);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    // re-number the wcResult (aka scenario) IDs as they can be a little wacky for some reason
    int id = 1;
    List<WcResult> wcResults = worstCaseTrLim.wcResults();
    for (WcResult wcResult : wcResults) {
      wcResult = new WcResult(wcResult.version(), String.valueOf(id), wcResult.title(), wcResult.buses(), wcResult.StressGens(), wcResult.flowgates(), wcResult.branchTerminalList(), wcResult.type(), wcResult.lineCostData(), wcResult.transformerCostData());
      worstCaseTrLim.wcResults().set(id - 1, wcResult);
      ++id;
    }

    return worstCaseTrLim;
  }
}
