package com.powergem.wce;

import com.powergem.sql.UncheckedConnection;
import com.powergem.sql.UncheckedResultSet;
import com.powergem.sql.UncheckedStatement;
import com.powergem.wce.entities.BusEntity;

import java.util.ArrayList;
import java.util.List;

public final class BusesTable implements AutoCloseable {
  private final UncheckedConnection connection;
  private final int scenarioId;

  public BusesTable(UncheckedConnection connection, int scenarioId) {
    this.connection = connection;
    this.scenarioId = scenarioId;
  }

  public List<BusEntity> toList() {
    List<BusEntity> buses = new ArrayList<>();
    try (UncheckedStatement statement = this.connection.createStatement()) {
      try (UncheckedResultSet rs = statement.executeQuery("select * from buses where scenarioId = " + this.scenarioId)) {
        while (rs.next()) {
          buses.add(DataFile.toBus(rs));
        }
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return buses;
  }

  @Override
  public void close() throws Exception {

  }
}
