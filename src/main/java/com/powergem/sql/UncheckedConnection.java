package com.powergem.sql;

import java.sql.Connection;
import java.sql.SQLException;

public final class UncheckedConnection implements AutoCloseable {
  private final Connection connection;

  public UncheckedConnection(Connection connection) {
    this.connection = connection;
  }

  @Override
  public void close() throws Exception {
    this.connection.close();
  }

  public UncheckedStatement createStatement(int batchSize) {
    try {
      return new UncheckedStatement(connection.createStatement(), batchSize);
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }
  }

  public UncheckedPreparedStatement prepareStatement(String sql) {
    try {
      return new UncheckedPreparedStatement(connection.prepareStatement(sql));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
