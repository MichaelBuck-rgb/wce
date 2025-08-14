package com.powergem.wce;

import com.powergem.sql.UncheckedConnection;
import com.powergem.sql.UncheckedResultSet;
import com.powergem.sql.UncheckedStatement;
import com.powergem.wce.entities.StressGenEntity;

import java.util.ArrayList;
import java.util.List;

public final class StressGensTable implements AutoCloseable {
  private final UncheckedConnection connection;
  private final int scenarioId;

  public StressGensTable(UncheckedConnection connection, int scenarioId) {
    this.connection = connection;
    this.scenarioId = scenarioId;
  }

  public List<StressGenEntity> toList() {
    List<StressGenEntity> entities = new ArrayList<>();
    try (UncheckedStatement statement = this.connection.createStatement(); UncheckedResultSet rs = statement.executeQuery("SELECT * FROM stressgens WHERE scenarioId = " + this.scenarioId)) {
      while (rs.next()) {
        entities.add(
                new StressGenEntity(
                        scenarioId,
                        rs.getInt("id").orElseThrow(),
                        rs.getInt("busnum").orElseThrow(),
                        rs.getString("busname").orElseThrow(),
                        rs.getDouble("busvolt").orElseThrow(),
                        rs.getString("busarea").orElseThrow(),
                        rs.getDouble("lat").orElseThrow(),
                        rs.getDouble("lon").orElseThrow()
                )
        );
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return entities;
  }

  @Override
  public void close() throws Exception {

  }
}
