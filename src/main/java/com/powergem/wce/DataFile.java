package com.powergem.wce;

import com.powergem.sql.UncheckedConnection;
import com.powergem.sql.UncheckedPreparedStatement;
import com.powergem.sql.UncheckedResultSet;
import com.powergem.sql.UncheckedSQLException;
import com.powergem.wce.entities.BusEntity;
import com.powergem.wce.entities.ConstraintsEntity;
import com.powergem.wce.entities.FlowgateEntity;
import com.powergem.wce.entities.HarmerEntity;
import com.powergem.worstcasetrlim.model.BranchTerminal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class DataFile {
  private final UncheckedConnection connection;

  public DataFile(UncheckedConnection connection) {
    this.connection = connection;
  }

  public Optional<BusEntity> getBus(int busNum) {
    try (UncheckedPreparedStatement statement = connection.prepareStatement("select * from buses where busnum = ?")) {
      statement.setInt(1, busNum);
      try (UncheckedResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          return Optional.of(toBus(resultSet));
        }
        return Optional.empty();
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public List<FlowgateEntity> getFlowgates(int scenarioId, int busNum) {
    int busid = getBus(busNum).orElseThrow()
            .id();

    List<FlowgateEntity> flowgates = new ArrayList<>();
    try (UncheckedPreparedStatement statement = connection.prepareStatement("select * from flowgates where scenarioId = ? and busid = ?")) {
      statement.setInt(1, scenarioId);
      statement.setInt(2, busid);
      try (UncheckedResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          FlowgateEntity flowgateEntity = getFlowgateEntity(resultSet);
          flowgates.add(flowgateEntity);
        }
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return flowgates;
  }

  public List<HarmerEntity> getHarmers(int scenarioId, int flowgateId) {
    List<HarmerEntity> flowgates = new ArrayList<>();
    try (UncheckedPreparedStatement statement = connection.prepareStatement("select * from harmers where scenarioId = ? and flowgateId = ?")) {
      statement.setInt(1, scenarioId);
      statement.setInt(2, flowgateId);
      try (UncheckedResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          HarmerEntity flowgateEntity = getHarmers(resultSet);
          flowgates.add(flowgateEntity);
        }
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return flowgates;
  }

  private static FlowgateEntity getFlowgateEntity(UncheckedResultSet resultSet) {
    return new FlowgateEntity(
            resultSet.getInt("scenarioId").orElseThrow(),
            resultSet.getInt("id").orElseThrow(),
            resultSet.getInt("busid").orElseThrow(),
            resultSet.getDouble("dfax").orElseThrow(),
            resultSet.getDouble("trlim").orElseThrow(),
            resultSet.getString("mon").orElseThrow(),
            resultSet.getString("con").orElseThrow(),
            resultSet.getDouble("rating").orElseThrow(),
            resultSet.getDouble("loadingbefore").orElseThrow()
    );
  }

  private static BusEntity toBus(UncheckedResultSet resultSet) {
    return new BusEntity(
            resultSet.getInt("scenarioId").orElseThrow(),
            resultSet.getInt("id").orElseThrow(),
            resultSet.getInt("busnum").orElseThrow(),
            resultSet.getString("busname").orElseThrow(),
            resultSet.getDouble("busvolt").orElseThrow(),
            resultSet.getString("busarea").orElseThrow(),
            resultSet.getDouble("trlim").orElseThrow(),
            resultSet.getDouble("lat").orElseThrow(),
            resultSet.getDouble("lon").orElseThrow()
    );
  }

  private HarmerEntity getHarmers(UncheckedResultSet resultSet) throws SQLException {
    return new HarmerEntity(
            resultSet.getInt("id").orElseThrow(),
            resultSet.getInt("flowgateId").orElseThrow(),
            resultSet.getInt("stressGenId").orElseThrow(),
            resultSet.getDouble("dfax").orElseThrow(),
            resultSet.getDouble("mwchange").orElseThrow(),
            resultSet.getDouble("mwimpact").orElseThrow(),
            resultSet.getDouble("pmax").orElseThrow(),
            resultSet.getDouble("pgen").orElseThrow(),
            resultSet.getInt("scenarioId").orElseThrow()
    );
  }

  public List<ConstraintsEntity> getConstraints(int scenarioId, int flowgateId) {
    List<ConstraintsEntity> contraints = new ArrayList<>();
    try (UncheckedPreparedStatement statement = connection.prepareStatement("select * from constraints where scenarioId = ? and flowgateId = ?")) {
      statement.setInt(1, scenarioId);
      statement.setInt(2, flowgateId);
      try (UncheckedResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          ConstraintsEntity constraintsEntity = getConstraint(resultSet);
          contraints.add(constraintsEntity);
        }
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return contraints;
  }

  private ConstraintsEntity getConstraint(UncheckedResultSet resultSet) {
    return new ConstraintsEntity(
            resultSet.getInt("scenarioId").orElseThrow(),
            resultSet.getInt("flowgateid").orElseThrow(),
            resultSet.getInt("montype").orElseThrow(),
            resultSet.getInt("frbus").orElseThrow(),
            resultSet.getInt("tobus").orElseThrow()
    );
  }

  public Optional<BranchTerminal> getBranchBus(int scenarioId, int id) {
    try (UncheckedPreparedStatement statement = connection.prepareStatement("select * from branchterminals where scenarioId = ? and id = ?")) {
      statement.setInt(1, scenarioId);
      statement.setInt(2, id);
      try (UncheckedResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          return Optional.of(toBranchTerminal(resultSet));
        }
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return Optional.empty();
  }

  private BranchTerminal toBranchTerminal(UncheckedResultSet resultSet) {
    return new BranchTerminal(
            resultSet.getInt("id").orElseThrow(),
            resultSet.getString("name").orElseThrow(),
            resultSet.getDouble("kv").orElseThrow(),
            resultSet.getInt("areanum").orElseThrow(),
            resultSet.getString("areaname").orElseThrow(),
            resultSet.getDouble("lat").orElseThrow(),
            resultSet.getDouble("lon").orElseThrow()
    );
  }

  public Optional<FlowgateEntity> getFlowgateById(int id, int scenarioId) {
    try (UncheckedPreparedStatement statement = connection.prepareStatement("select * from flowgates where scenarioId = ? and id = ?")) {
      statement.setInt(1, scenarioId);
      statement.setInt(2, id);
      try (UncheckedResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          return Optional.of(getFlowgateEntity(resultSet));
        }
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return Optional.empty();
  }
}
