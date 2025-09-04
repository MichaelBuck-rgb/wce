package com.powergem.sql;

import java.sql.SQLException;
import java.sql.Statement;

public final class UncheckedStatement implements AutoCloseable {
  private final Statement statement;

  public UncheckedStatement(Statement statement) {
    this.statement = statement;
  }

  @Override
  public void close() throws Exception {
    this.statement.close();
  }

  public boolean execute(String sql) {
    try {
      return statement.execute(sql);
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }
  }
}
