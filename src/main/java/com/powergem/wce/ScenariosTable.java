package com.powergem.wce;

import com.powergem.sql.UncheckedConnection;
import com.powergem.sql.UncheckedResultSet;
import com.powergem.sql.UncheckedStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class ScenariosTable implements AutoCloseable {
  private final UncheckedConnection connection;

  public ScenariosTable(UncheckedConnection connection) {
    this.connection = connection;
  }



  public long count() {
    return this.connection.getLong("select count(*) from scenarios").orElseThrow();
  }

  public Stream<ScenarioTable> scenarios() {
    List<ScenarioTable> scenarios = new ArrayList<>();
    try (UncheckedStatement statement = this.connection.createStatement(); UncheckedResultSet rs = statement.executeQuery("select id from scenarios")) {
      while (rs.next()) {
        scenarios.add(new ScenarioTable(this.connection, rs.getInt(1).orElseThrow()));
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return scenarios.stream();
  }

  @Override
  public void close() throws Exception {

  }
}
