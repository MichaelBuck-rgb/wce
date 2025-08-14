package com.powergem.sql;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public final class UncheckedStatement implements AutoCloseable {
  private final Statement statement;

  public static final class Batcher implements Consumer<String>, AutoCloseable {
    private final UncheckedStatement statement;
    private final int batchSize;
    private int counter;

    public Batcher(UncheckedStatement statement, int batchSize) {
      this.statement = statement;
      this.batchSize = batchSize;
    }

    @Override
    public void close() throws Exception {
      this.statement.close();
    }

    @Override
    public void accept(String s) {
      this.statement.addBatch(s);
      this.counter++;
      if (this.counter >= this.batchSize) {
        this.statement.executeBatch();
        this.counter = 0;
      }
    }
  }

  private void executeBatch() {
    try {
      this.statement.executeBatch();
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }
  }

  private void addBatch(String sql) {
    try {
      this.statement.addBatch(sql);
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }
  }

  public UncheckedStatement(Statement statement) {
    this.statement = statement;
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

  public OptionalLong getLong(String query) {
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

  public <T> List<T> executeQuery(String query, Function<UncheckedResultSet, T> mapper) {
    List<T> list = new ArrayList<>();
    try (UncheckedResultSet resultSet = new UncheckedResultSet(this.statement.executeQuery(query))) {
      while (resultSet.next()) {
        T value = mapper.apply(resultSet);
        list.add(value);
      }
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return list;
  }

//  public void executeQuery(String query, BiConsumer<UncheckedResultSet, Long> consumer) {
//    try (UncheckedResultSet rs = executeQuery(query)) {
//      long index = 0;
//      while (rs.next()) {
//        consumer.accept(rs, index++);
//      }
//    } catch (RuntimeException e) {
//      throw e;
//    } catch (Exception e) {
//      throw new RuntimeException(e);
//    }
//  }

//  public int executeUpdate(String sql) {
//    try {
//      return statement.executeUpdate(sql);
//    } catch (SQLException e) {
//      throw new UncheckedSQLException(e);
//    }
//  }
//
//  public long getGeneratedKeys() {
//    try (ResultSet rs = statement.getGeneratedKeys()) {
//      if (rs.next()) {
//        return rs.getLong(1);
//      } else {
//        throw new IllegalStateException("No rows returned");
//      }
//    } catch (SQLException e) {
//      throw new UncheckedSQLException(e);
//    }
//  }

  public boolean execute(String sql) {
    try {
      return statement.execute(sql);
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }
  }

//  public void addBatch(String sql) {
//    try {
//      statement.addBatch(sql);
//    } catch (SQLException e) {
//      throw new UncheckedSQLException(e);
//    }
//
//    if (++batchIndex == batchSize) {
//      executeBatch();
//      batchIndex = 0;
//    }
//  }

//  public void executeBatch() {
//    try {
//      statement.executeBatch();
//    } catch (SQLException e) {
//      throw new UncheckedSQLException(e);
//    }
//  }

  public Optional<String> getString(String query) {
    try (UncheckedResultSet rs = executeQuery(query)) {
      if (!rs.next()) {
        return Optional.empty();
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

//  public Optional<String> getCreateStatement(String table) {
//    return getString("SELECT sql FROM sqlite_master WHERE type='table' AND name='%s'".formatted(table));
//  }
//
//  public OptionalDouble getDouble(String query) {
//    try (ResultSet rs = statement.executeQuery(query)) {
//      if (!rs.next()) {
//        throw new IllegalStateException("No rows returned");
//      }
//      double aDouble = rs.getDouble(1);
//      if (rs.wasNull()) {
//        return OptionalDouble.empty();
//      }
//      return OptionalDouble.of(aDouble);
//    } catch (SQLException e) {
//      throw new UncheckedSQLException(e);
//    }
//  }
}
