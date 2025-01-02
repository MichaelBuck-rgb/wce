package com.powergem.worstcasetrlim;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.powergem.worstcasetrlim.model.WorstCaseTrLim;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Utilities {

  private Utilities() {
  }

  public static WorstCaseTrLim getWorstCaseTrLim(Path file) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setDefaultSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));

    ObjectReader objectReader = objectMapper.readerFor(WorstCaseTrLim.class);

    try (InputStream inputStream = Files.newInputStream(file)) {
      return objectReader.readValue(inputStream);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
