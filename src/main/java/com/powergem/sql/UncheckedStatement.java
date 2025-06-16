package com.powergem.sql;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.function.BiConsumer;

public final class UncheckedStatement implements AutoCloseable {
  private final Statement statement;
  private final int batchSize;
  private int batchIndex;

  public UncheckedStatement(Statement statement, int batchSize) {
    this.statement = statement;
    this.batchSize = batchSize;
  }

  public int getInt(String query) {
    try (ResultSet rs = statement.executeQuery(query)) {
      if (!rs.next()) {
        throw new IllegalStateException("No rows returned");
      }
      return rs.getInt(1);
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }
  }

  public long getLong(String query) {
    try (UncheckedResultSet rs = executeQuery(query)) {
      if (!rs.next()) {
        throw new IllegalStateException("No rows returned");
      }
      return rs.getLong(1);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() throws Exception {
    this.statement.close();
  }

  public UncheckedResultSet executeQuery(String query) {
    try {
      ResultSet resultSet = statement.executeQuery(query);
      return new UncheckedResultSet(resultSet);
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }
  }

  public void executeQuery(String query, BiConsumer<UncheckedResultSet, Long> consumer) {
    try (UncheckedResultSet rs = executeQuery(query)) {
      long index = 0;
      while (rs.next()) {
        consumer.accept(rs, index++);
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int executeUpdate(String sql) {
    try {
      return statement.executeUpdate(sql);
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }
  }

  public long getGeneratedKeys() {
    try (ResultSet rs = statement.getGeneratedKeys()) {
      if (rs.next()) {
        return rs.getLong(1);
      } else {
        throw new IllegalStateException("No rows returned");
      }
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }
  }

  public boolean execute(String sql) {
    try {
      return statement.execute(sql);
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }
  }

  public void addBatch(String sql) {
    try {
      statement.addBatch(sql);
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }

    if (++batchIndex == batchSize) {
      executeBatch();
      batchIndex = 0;
    }
  }

  public void executeBatch() {
    try {
      statement.executeBatch();
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }
  }

  public Optional<String> getString(String query) {
    try (UncheckedResultSet rs = executeQuery(query)) {
      if (!rs.next()) {
        throw new IllegalStateException("No rows returned");
      }
      return rs.getString(1);
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Optional<String> getCreateStatement(String table) {
    return getString("SELECT sql FROM sqlite_master WHERE type='table' AND name='%s'".formatted(table));
  }

}
