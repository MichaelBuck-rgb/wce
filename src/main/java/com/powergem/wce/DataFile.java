package com.powergem.wce;

import com.powergem.TableBuilder;
import com.powergem.sql.*;
import com.powergem.wce.entities.*;
import com.powergem.worstcasetrlim.model.*;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public final class DataFile {
  private final UncheckedConnection connection;

  public DataFile(UncheckedConnection connection) {
    this.connection = connection;
  }

  public List<ScenarioEntity> getScenarios() {
    try (UncheckedPreparedStatement statement = connection.prepareStatement("select * from scenarios")) {
      try (UncheckedResultSet resultSet = statement.executeQuery()) {
        List<ScenarioEntity> scenarios = new ArrayList<>();
        while (resultSet.next()) {
          scenarios.add(toScenario(resultSet));
        }
        return scenarios;
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private ScenarioEntity toScenario(UncheckedResultSet resultSet) {
    return new ScenarioEntity(
            Integer.parseInt(resultSet.getString("id").orElseThrow()),
            resultSet.getString("name").orElseThrow(),
            resultSet.getString("version").orElseThrow(),
            resultSet.getString("mode").orElseThrow()
    );
  }

  public Optional<BusEntity> getBus(int busNum, int scenarioId) {
    try (UncheckedPreparedStatement statement = connection.prepareStatement("select * from buses where scenarioId = ? and busnum = ?")) {
      statement.setInt(1, scenarioId);
      statement.setInt(2, busNum);
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
    int busid = getBus(busNum, scenarioId).orElseThrow()
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
            resultSet.getString("mon").orElseThrow().trim(),
            resultSet.getString("con").orElseThrow().trim(),
            resultSet.getDouble("rating").orElseThrow(),
            resultSet.getDouble("loadingbefore").orElseThrow(),
            resultSet.getInt("equipment_index")
    );
  }

  private static BusEntity toBus(UncheckedResultSet resultSet) {
    return new BusEntity(
            resultSet.getInt("scenarioId").orElseThrow(),
            resultSet.getInt("id").orElseThrow(),
            resultSet.getInt("busnum").orElseThrow(),
            resultSet.getString("busname").orElseThrow().trim(),
            resultSet.getDouble("busvolt").orElseThrow(),
            resultSet.getString("busarea").orElseThrow().trim(),
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
            resultSet.getString("name").orElseThrow().trim(),
            resultSet.getDouble("kv").orElseThrow(),
            resultSet.getInt("areanum").orElseThrow(),
            resultSet.getString("areaname").orElseThrow().trim(),
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

  public Optional<LineCostDatumEntity> getLineCostDatumById(int id, int scenarioId) {
    try (UncheckedPreparedStatement statement = connection.prepareStatement("select * from line_cost_data where scenarioId = ? and id = ?")) {
      statement.setInt(1, scenarioId);
      statement.setInt(2, id);
      try (UncheckedResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          return Optional.of(getLineCostDatum(resultSet));
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

  private LineCostDatumEntity getLineCostDatum(UncheckedResultSet resultSet) {
    return new LineCostDatumEntity(
            resultSet.getInt("scenarioId").orElseThrow(),
            resultSet.getInt("id").orElseThrow(),
            resultSet.getFloat("length").orElseThrow(),
            resultSet.getFloat("max_rating_per_line").orElseThrow(),
            resultSet.getFloat("max_allowed_flow_per_line").orElseThrow(),
            resultSet.getFloat("upgrade_cost").orElseThrow(),
            resultSet.getFloat("new_line_cost").orElseThrow()
    );
  }


  public void search(String search, UncheckedConnection connection) {
    record Tuple(ScenarioEntity scenarioEntity, Collection<BusEntity> buses) {
    }

    TableBuilder tableBuilder = new TableBuilder(Utilities.busHeader());
    getScenarios().stream()
            .map(scenarioEntity -> new Tuple(scenarioEntity, searchForBuses(search, scenarioEntity.id(), connection)))
            .forEach(tuple -> tuple.buses.forEach(busEntity -> tableBuilder.addRow(
                    tuple.scenarioEntity.name(),
                    String.valueOf(busEntity.id()),
                    String.valueOf(busEntity.busnum()),
                    busEntity.busname(),
                    String.valueOf(busEntity.busvolt()),
                    busEntity.busarea(),
                    String.format("%.2f", busEntity.trlim()),
                    String.format("(%.6f, %.6f)", busEntity.lat(), busEntity.lon())
            )));

    System.out.printf("Found '%s' in the following buses:%n", search);
    tableBuilder.printTable();
  }

  public Collection<BusEntity> searchForBuses(String search, int scenarioId, UncheckedConnection connection) {
    Collection<BusEntity> buses = new ArrayList<>();

    String escapedSearch = search.replace("!", "!!")
            .replace("%", "!%")
            .replace("_", "!_")
            .replace("[", "![");
    try (UncheckedPreparedStatement statement = connection.prepareStatement("SELECT * FROM buses WHERE scenarioId = ? AND (UPPER(busname) LIKE ? ESCAPE '!' OR UPPER(busarea) LIKE ? ESCAPE '!')")) {
      statement.setInt(1, scenarioId);
      statement.setString(2, "%" + escapedSearch + "%");
      statement.setString(3, "%" + escapedSearch + "%");

      try (UncheckedResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          BusEntity bus = toBus(resultSet);
          buses.add(bus);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return buses;
  }

  public void extractBuses(List<Integer> busNums, UncheckedConnection uncheckedConnection) {
    Map<Integer, List<ScenarioEntity>> scenarioGroups = getScenarios().stream().collect(groupingBy(ScenarioEntity::id));

    Map<Integer, List<BusEntity>> busesGroupedByScenario = getBuses(busNums, uncheckedConnection).stream().collect(groupingBy(BusEntity::scenarioId));

    Map<Integer, List<FlowgateEntity>> flowgatesGroupedByScenario = busesGroupedByScenario.entrySet().stream()
            .flatMap(entry -> entry.getValue().stream()
                    .flatMap(busEntity -> getFlowgates(entry.getKey(), busEntity.busnum()).stream()))
            .collect(groupingBy(FlowgateEntity::scenarioId));

    List<WcResult> wcResults = busesGroupedByScenario.entrySet().stream()
            .map(entry -> {
              ScenarioEntity scenarioEntity = scenarioGroups.get(entry.getKey()).getFirst();
              List<Bus> buses = entry.getValue().stream().map(bus -> new Bus(bus.id(), bus.busnum(), bus.busname(), bus.busvolt(), bus.busarea(), bus.trlim(), bus.lat(), bus.lon())).toList();
              return Map.entry(scenarioEntity, buses);
            })
            .map(entry -> {
              ScenarioEntity scenarioEntity = entry.getKey();
              List<Flowgate> flowgates = flowgatesGroupedByScenario.get(scenarioEntity.id()).stream()
                      .map(flowgateEntity -> new Flowgate(flowgateEntity.id(), flowgateEntity.busId(), flowgateEntity.dfax(), flowgateEntity.trlim(), flowgateEntity.mon(), flowgateEntity.con(), flowgateEntity.rating(), flowgateEntity.loadingBefore(), flowgateEntity.loadingBefore(), 0, List.of(), null, null, null, null))
                      .toList();
              return new WcResult(scenarioEntity.version(), String.valueOf(scenarioEntity.id()), scenarioEntity.name(), entry.getValue(), List.of(), flowgates, List.of(), scenarioEntity.mode(), List.of(), List.of());
            })
            .toList();

    WorstCaseTrLim worstCaseTrLim = new WorstCaseTrLim(wcResults);

    Jsonb jsonb = Jsonb.builder().build();
    JsonType<WorstCaseTrLim> customerType = jsonb.type(WorstCaseTrLim.class);
    customerType.toJson(worstCaseTrLim, System.out);
  }

  public enum BusOrderBy {
    NONE,
    NUM,
    NAME,
    AREA,
    VOLTAGE,
  }

  public List<BusEntity> getBuses(List<Integer> busNums, UncheckedConnection uncheckedConnection) {
    List<BusEntity> buses = new ArrayList<>();

    String inValues = busNums.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));

    try (UncheckedPreparedStatement statement = uncheckedConnection.prepareStatement(
            "SELECT * FROM buses WHERE busnum IN (" + inValues + ")")) {
      try (UncheckedResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          BusEntity bus = toBus(resultSet);
          buses.add(bus);
        }
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return buses;
  }

  public List<BusEntity> getBuses(int scenarioId, BusOrderBy orderBy) {
    String query = "select * from buses where scenarioId = ? " + switch (orderBy) {
      case NONE -> "";
      case NUM -> "order by busnum";
      case NAME -> "order by busname";
      case AREA -> "order by busarea";
      case VOLTAGE -> "order by busvolt";
    };

    List<BusEntity> buses = new ArrayList<>();
    try (UncheckedPreparedStatement statement = connection.prepareStatement(query)) {
      statement.setInt(1, scenarioId);
      try (UncheckedResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          BusEntity bus = toBus(resultSet);
          buses.add(bus);
        }
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return buses;
  }
}
