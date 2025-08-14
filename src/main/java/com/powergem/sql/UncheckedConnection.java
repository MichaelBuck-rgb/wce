package com.powergem.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.OptionalLong;

public final class UncheckedConnection implements AutoCloseable {
  private final Connection connection;

  public UncheckedConnection(Connection connection) {
    try {
      connection.setAutoCommit(false);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    this.connection = connection;
  }

  @Override
  public void close() throws Exception {
    this.connection.close();
  }

  public UncheckedStatement createStatement() {
    try {
      return new UncheckedStatement(connection.createStatement());
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

  public OptionalLong getLong(String query) {
    try (Statement statement = this.connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {
      if (resultSet.next()) {
        long aLong = resultSet.getLong(1);
        return resultSet.wasNull() ? OptionalLong.empty() : OptionalLong.of(aLong);
      }
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }
    return OptionalLong.empty();
  }

  public Optional<String> getString(String query) {
    try (Statement statement = this.connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {
      if (resultSet.next()) {
        String aString = resultSet.getString(1);
        return resultSet.wasNull() ? Optional.empty() : Optional.of(aString);
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
