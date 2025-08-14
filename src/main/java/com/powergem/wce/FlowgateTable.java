package com.powergem.wce;

import com.powergem.sql.UncheckedConnection;
import com.powergem.sql.UncheckedPreparedStatement;
import com.powergem.sql.UncheckedResultSet;
import com.powergem.sql.UncheckedStatement;
import com.powergem.wce.entities.FlowgateEntity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import static com.powergem.wce.DataFile.getFlowgateEntity;

public final class FlowgateTable implements AutoCloseable {
  private static final String UPDATE_TEMPLATE = "UPDATE flowgates SET scenarioId=?, id=?, busid=?, dfax=?, trlim=?, mon=?, con=?, rating=?, loadingbefore=?, equipment_index=? WHERE scenarioId = ? and id = ?";

  private final UncheckedConnection connection;
  private final int scenarioId;

  FlowgateTable(UncheckedConnection connection, int scenarioId) {
    this.connection = connection;
    this.scenarioId = scenarioId;
  }

  public long getCount() {
    return this.connection.getLong("select count(*) from flowgates where scenarioId = " + this.scenarioId).orElseThrow();
  }

  public static final class Updater implements Consumer<FlowgateEntity>, AutoCloseable {
    private final int scenarioId;
    private final UncheckedPreparedStatement statement;
    private final int batchSize;
    private int counter;

    public Updater(int batchSize, int scenarioId, UncheckedPreparedStatement statement) {
      this.batchSize = batchSize;
      this.scenarioId = scenarioId;
      this.statement = statement;
    }

    @Override
    public void close() throws Exception {
      this.statement.executeBatch();
      this.statement.getConnection().commit();
      this.statement.close();
    }

    @Override
    public void accept(FlowgateEntity flowgateEntity) {
      // todo: don't need to update scenarioId and id
      statement.setInt(1, this.scenarioId);
      statement.setInt(2, flowgateEntity.id());
      statement.setInt(3, flowgateEntity.busid());
      statement.setDouble(4, flowgateEntity.dfax());
      statement.setDouble(5, flowgateEntity.trlim());
      statement.setString(6, flowgateEntity.mon());
      statement.setString(7, flowgateEntity.con());
      statement.setDouble(8, flowgateEntity.rating());
      statement.setDouble(9, flowgateEntity.loadingbefore());

      if (flowgateEntity.equipment_index().isPresent()) {
        statement.setInt(10, flowgateEntity.equipment_index().get());
      } else {
        statement.setNull(10, java.sql.Types.INTEGER);
      }

      statement.setInt(11, this.scenarioId);
      statement.setInt(12, flowgateEntity.id());

      statement.addBatch();

      if (this.counter++ >= this.batchSize) {
        statement.executeBatch();
        this.statement.getConnection().commit();
        this.counter = 0;
      }
    }
  }

  public Updater updater(int batchSize) {
    UncheckedPreparedStatement statement = this.connection.prepareStatement(UPDATE_TEMPLATE);

    return new Updater(batchSize, this.scenarioId, statement);
  }

  @Override
  public void close() {

  }

  public List<FlowgateEntity> toList() {
    List<FlowgateEntity> entities = new ArrayList<>();
    try (UncheckedStatement statement = this.connection.createStatement(); UncheckedResultSet rs = statement.executeQuery("select * from flowgates where scenarioId = " + this.scenarioId)) {
      new FlowgateIterator(rs).forEachRemaining(entities::add);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return entities;
  }

  private static final class FlowgateIterator implements Iterator<FlowgateEntity> {
    private final UncheckedResultSet rs;
    private FlowgateEntity next;

    public FlowgateIterator(UncheckedResultSet rs) {
      this.rs = rs;
      _next();
    }

    private void _next() {
      if (rs.next()) {
        this.next = getFlowgateEntity(this.rs);
      } else {
        this.next = null;
        try {
          rs.close();
        } catch (Exception _) {
        }
      }
    }

    @Override
    public boolean hasNext() {
      return this.next != null;
    }

    @Override
    public FlowgateEntity next() {
      FlowgateEntity next = this.next;

      _next();

      return next;
    }
  }
}
