package com.powergem.wce;

import com.powergem.sql.UncheckedConnection;
import com.powergem.sql.UncheckedResultSet;
import com.powergem.sql.UncheckedStatement;

import java.util.*;
import java.util.function.Function;

public final class ScenariosTable implements AutoCloseable {
  private final UncheckedConnection connection;

  public ScenariosTable(UncheckedConnection connection) {
    this.connection = connection;
  }

  public List<ScenarioTable> getScenarioTables() {
    List<ScenarioTable> scenarioTable = new ArrayList<>();
    try (UncheckedStatement statement = this.connection.createStatement(); UncheckedResultSet rs = statement.executeQuery("select scenarioId from scenarios")) {
      while (rs.next()) {
        scenarioTable.add(new ScenarioTable(this.connection, rs.getInt(1).orElseThrow()));
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return scenarioTable;
  }

  @Override
  public void close() throws Exception {

  }

  public void reIndexScenarios() {
    try (UncheckedStatement statement = this.connection.createStatement()) {
      List<Integer> ids = statement.executeQuery("select scenarioId from scenarios order by scenarioId", new Function<UncheckedResultSet, Integer>() {
        @Override
        public Integer apply(UncheckedResultSet rs) {
          return rs.getInt(1).orElseThrow();
        }
      });

      Map<Integer, Integer> scenarioIdMap = new HashMap<>();
      for (int i = 0; i < ids.size(); i++) {
        scenarioIdMap.put(ids.get(i), i + 7);
      }

      renumberScenarioIdColumn("scenarios", scenarioIdMap, statement);
      renumberScenarioIdColumn("branchterminals", scenarioIdMap, statement);
      renumberScenarioIdColumn("buses", scenarioIdMap, statement);
      renumberScenarioIdColumn("constraints", scenarioIdMap, statement);
      renumberScenarioIdColumn("flowgates", scenarioIdMap, statement);
      renumberScenarioIdColumn("harmers", scenarioIdMap, statement);
      renumberScenarioIdColumn("line_cost_data", scenarioIdMap, statement);
      renumberScenarioIdColumn("stressgens", scenarioIdMap, statement);
      renumberScenarioIdColumn("transformer_cost_data", scenarioIdMap, statement);

    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void renumberScenarioIdColumn(String tableName, Map<Integer, Integer> scenarioIdMap, UncheckedStatement statement) {
    record Tuple(String indexName, String createStatement) {
    }

    List<Tuple> tuples = statement.executeQuery("SELECT name, sql FROM sqlite_master WHERE type = 'index' AND tbl_name = '%s'".formatted(tableName), new Function<UncheckedResultSet, Tuple>() {
      @Override
      public Tuple apply(UncheckedResultSet uncheckedResultSet) {
        return new Tuple(uncheckedResultSet.getString(1).orElseThrow(), uncheckedResultSet.getString(2).orElseThrow());
      }
    });

    // drop the indices
    tuples.stream().map(tuple -> tuple.indexName)
            .map("DROP INDEX %s"::formatted)
            .forEach(statement::execute);

    statement.execute("ALTER TABLE %s ADD COLUMN _scenarioId INTEGER".formatted(tableName));

    for (Map.Entry<Integer, Integer> entry : scenarioIdMap.entrySet()) {
      statement.execute("UPDATE %s set _scenarioId = %d where scenarioId = %d".formatted(tableName, entry.getValue(), entry.getKey()));
    }

    statement.execute("ALTER TABLE %s DROP COLUMN scenarioId".formatted(tableName));
    statement.execute("ALTER TABLE %s RENAME COLUMN _scenarioId TO scenarioId".formatted(tableName));

    // recreate indices
    tuples.stream().map(tuple -> tuple.createStatement)
            .forEach(statement::execute);

    this.connection.commit();
  }
}
