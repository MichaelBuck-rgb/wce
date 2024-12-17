package com.powergem.wce;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public final class Utils {

  private Utils() {
  }

  public static Connection getConnection(String jdbcUrl) {
    Properties properties = new Properties();
    properties.put("enable_load_extension", "true");

    try {
      Connection connection = DriverManager.getConnection(jdbcUrl, properties);
      try (Statement statement = connection.createStatement()) {
        statement.execute("PRAGMA synchronous = OFF");
        statement.execute("PRAGMA journal_mode = MEMORY");
      }
      return connection;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
