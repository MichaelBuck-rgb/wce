package com.powergem.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class UncheckedPreparedStatement implements AutoCloseable{
  private final PreparedStatement statement;

  public UncheckedPreparedStatement(PreparedStatement statement) {
    this.statement = statement;
  }

  public void setInt(int parameterIndex, int x) {
    try {
      this.statement.setInt(parameterIndex, x);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() throws Exception {
    this.statement.close();
  }

  public UncheckedResultSet executeQuery() {
    try {
      return new UncheckedResultSet(this.statement.executeQuery());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
