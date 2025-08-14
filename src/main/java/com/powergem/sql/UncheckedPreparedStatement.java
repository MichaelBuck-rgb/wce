package com.powergem.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class UncheckedPreparedStatement implements AutoCloseable {
  private final UncheckedConnection connection;
  private final PreparedStatement statement;

  UncheckedPreparedStatement(UncheckedConnection connection, PreparedStatement statement) {
    this.connection = connection;
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

  public void setString(int parameterIndex, String x) {
    try {
      this.statement.setString(parameterIndex, x);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void setDouble(int parameterIndex, double x) {
    try {
      this.statement.setDouble(parameterIndex, x);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public int executeUpdate() {
    try {
      return this.statement.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void setNull(int parameterIndex, int sqlType) {
    try {
      this.statement.setNull(parameterIndex, sqlType);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void addBatch() {
    try {
      this.statement.addBatch();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void executeBatch() {
    try {
      this.statement.executeBatch();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public UncheckedConnection getConnection() {
    return this.connection;
  }
}
