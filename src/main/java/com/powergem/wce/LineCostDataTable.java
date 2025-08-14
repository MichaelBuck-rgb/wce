package com.powergem.wce;

import com.powergem.sql.UncheckedConnection;
import com.powergem.sql.UncheckedPreparedStatement;
import com.powergem.sql.UncheckedStatement;
import com.powergem.wce.entities.LineCostDatumEntity;

import java.util.List;

public final class LineCostDataTable implements AutoCloseable {
  private static final String INSERT_TEMPLATE = "INSERT INTO line_cost_data (scenarioId, id, length, max_rating_per_line, max_allowed_flow_per_line, upgrade_cost, new_line_cost) VALUES (?, ?, ?, ?, ?, ?, ?)";

  private final UncheckedConnection connection;
  private final UncheckedPreparedStatement statement;
  private final int scenarioId;

  LineCostDataTable(UncheckedConnection connection, int scenarioId) {
    this.connection = connection;
    this.statement = connection.prepareStatement(INSERT_TEMPLATE);
    this.scenarioId = scenarioId;
  }

  public void insertBatch(LineCostDatumEntity lineCostDatumEntity) {
    this.statement.setInt(1, this.scenarioId);
    this.statement.setInt(2, lineCostDatumEntity.id());
    this.statement.setDouble(3, lineCostDatumEntity.length());
    this.statement.setDouble(4, lineCostDatumEntity.maxRatingPerLine());
    this.statement.setDouble(5, lineCostDatumEntity.maxAllowedFlowPerLine());
    this.statement.setDouble(6, lineCostDatumEntity.upgradeCost());
    this.statement.setDouble(7, lineCostDatumEntity.newLineCost());
    this.statement.addBatch();
  }

  @Override
  public void close() throws Exception {
    this.statement.close();
  }

  public List<LineCostDatumEntity> getLineCostData() {
    try (UncheckedStatement statement1 = this.connection.createStatement()) {
      return statement1.executeQuery("select * from line_cost_data where scenarioId = " + this.scenarioId, rs -> new LineCostDatumEntity(
              LineCostDataTable.this.scenarioId,
              rs.getInt("id").orElseThrow(),
              rs.getDouble("length").orElseThrow(),
              rs.getDouble("max_rating_per_line").orElseThrow(),
              rs.getDouble("max_allowed_flow_per_line").orElseThrow(),
              rs.getDouble("upgrade_cost").orElseThrow(),
              rs.getDouble("new_line_cost").orElseThrow()));
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void executeBatch() {
    this.statement.executeBatch();
  }
}
