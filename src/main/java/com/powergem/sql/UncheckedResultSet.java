package com.powergem.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public final class UncheckedResultSet implements AutoCloseable {
  private final ResultSet resultSet;

  public UncheckedResultSet(ResultSet resultSet) {
    this.resultSet = resultSet;
  }

  public ResultSet getResultSet() {
    return resultSet;
  }

  @Override
  public void close() throws Exception {
    this.resultSet.close();
  }

  public ResultSetMetaData getMetaData() {
    try {
      return this.resultSet.getMetaData();
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }
  }

  public boolean next() {
    try {
      return this.resultSet.next();
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }
  }

  public Optional<String> getString(int columnIndex) {
    try {
      String value = this.resultSet.getString(columnIndex);
      if (resultSet.wasNull()) {
        return Optional.empty();
      }
      return Optional.of(value);
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }
  }

  public Optional<String> getString(String columnLabel) {
    try {
      String value = this.resultSet.getString(columnLabel);
      if (resultSet.wasNull()) {
        return Optional.empty();
      }
      return Optional.of(value);
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }
  }

  public long getLong(int columnIndex) {
    try {
      return resultSet.getLong(columnIndex);
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }
  }

  public long getLong(String columnLabel) {
    try {
      return resultSet.getLong(columnLabel);
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }
  }

  public byte[] getBytes(int columnIndex) {
    try {
      return resultSet.getBytes(columnIndex);
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }
  }

  public Optional<Object> getObject(int columnIndex) {
    try {
      Object value = resultSet.getObject(columnIndex);
      if (resultSet.wasNull()) {
        return Optional.empty();
      }
      return Optional.of(value);
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }
  }

  public int getColumnCount() {
    try {
      return resultSet.getMetaData().getColumnCount();
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }
  }

  public String getColumnName(int columnIndex) {
    try {
      return resultSet.getMetaData().getColumnName(columnIndex);
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }
  }

  public OptionalInt getInt(String columnLabel) {
    try {
      int anInt = this.resultSet.getInt(columnLabel);
      if (this.resultSet.wasNull()) {
        return OptionalInt.empty();
      }
      return OptionalInt.of(anInt);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public OptionalDouble getDouble(String columnLabel) {
    try {
      double aDouble = this.resultSet.getDouble(columnLabel);
      if (this.resultSet.wasNull()) {
        return OptionalDouble.empty();
      }
      return OptionalDouble.of(aDouble);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public Optional<Float> getFloat(String columnLabel) {
    try {
      float aFloat = this.resultSet.getFloat(columnLabel);
      if (this.resultSet.wasNull()) {
        return Optional.empty();
      }
      return Optional.of(aFloat);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
