package com.powergem.wce;

import com.powergem.sql.UncheckedConnection;
import com.powergem.sql.UncheckedResultSet;
import com.powergem.sql.UncheckedStatement;
import com.powergem.wce.entities.HarmerEntity;

import java.util.ArrayList;
import java.util.List;

public final class ScenarioTable implements AutoCloseable {
  private final UncheckedConnection connection;
  private final int scenarioId;

  ScenarioTable(UncheckedConnection connection, int scenarioId) {
    this.connection = connection;
    this.scenarioId = scenarioId;
  }

  public long getCount() {
    return this.connection.getLong("select count(*) from scenarios where scenarioId = " + this.scenarioId).orElseThrow();
  }

  public int getScenarioId() {
    return this.scenarioId;
  }

  public String getName() {
    return this.connection.getString("select name from scenarios where scenarioId = " + this.scenarioId).orElseThrow();
  }

  public String getMode() {
    return this.connection.getString("select mode from scenarios where scenarioId = " + this.scenarioId).orElseThrow();
  }

  public FlowgateTable getFlowgates() {
    return new FlowgateTable(this.connection, this.scenarioId);
  }

  @Override
  public void close() throws Exception {

  }

  public LineCostDataTable getLineCostData() {
    return new LineCostDataTable(this.connection, this.scenarioId);
  }

  public BusesTable getBuses() {
    return new BusesTable(this.connection, this.scenarioId);
  }

  public StressGensTable getStressgens() {
    return new StressGensTable(this.connection, this.scenarioId);
  }

  public List<HarmerEntity> getHarmers(int flowgateId) {
    try (UncheckedStatement statement = this.connection.createStatement()) {
      String query = "select * from harmers where scenarioId = " + this.scenarioId + " and flowgateId = " + flowgateId;
      return statement.executeQuery(query, DataFile::getHarmers);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public record ConstraintsInfo(int[] frBus, int[] toBus, int[] monType) {
  }

  public ConstraintsInfo getConstraintInfoForFlowgate(int flowgateId) {
    List<Integer> frbus = new ArrayList<>();
    List<Integer> tobus = new ArrayList<>();
    List<Integer> montype = new ArrayList<>();
    String query = "select frbus, tobus, montype from constraints where scenarioId = " + this.scenarioId + " and flowgateId = " + flowgateId;
    try (UncheckedStatement statement = this.connection.createStatement(); UncheckedResultSet rs = statement.executeQuery(query)) {
      while (rs.next()) {
        rs.getInt(1).ifPresent(frbus::add);
        rs.getInt(2).ifPresent(tobus::add);
        rs.getInt(3).ifPresent(montype::add);
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return new ConstraintsInfo(
            frbus.stream().mapToInt(Integer::intValue).toArray(),
            tobus.stream().mapToInt(Integer::intValue).toArray(),
            montype.stream().mapToInt(Integer::intValue).toArray()
    );
  }
}
