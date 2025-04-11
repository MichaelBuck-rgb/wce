package com.powergem.worstcasetrlim;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.powergem.worstcasetrlim.model.WcResult;
import com.powergem.worstcasetrlim.model.WorstCaseTrLim;

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
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setDefaultSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));

    ObjectReader objectReader = objectMapper.readerFor(WorstCaseTrLim.class);

    WorstCaseTrLim worstCaseTrLim;

    try (InputStream inputStream = Files.newInputStream(file)) {
      worstCaseTrLim = objectReader.readValue(inputStream);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    // re-number the wcResult (aka scenario) IDs as they can be a little wacky for some reason
    int id = 1;
    List<WcResult> wcResults = worstCaseTrLim.wcResults();
    for (WcResult wcResult : wcResults) {
      wcResult = new WcResult(wcResult.version(), String.valueOf(id), wcResult.title(), wcResult.buses(), wcResult.StressGens(), wcResult.flowgates(), wcResult.branchTerminalList());
      worstCaseTrLim.wcResults().set(id - 1, wcResult);
      ++id;
    }

    return worstCaseTrLim;
  }
}
