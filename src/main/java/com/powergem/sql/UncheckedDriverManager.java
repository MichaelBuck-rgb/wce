package com.powergem.sql;

import java.sql.DriverManager;
import java.sql.SQLException;

public final class UncheckedDriverManager {

  private UncheckedDriverManager() {
  }

  public static UncheckedConnection getConnection(String url) {
    try {
      UncheckedConnection uncheckedConnection = new UncheckedConnection(DriverManager.getConnection(url));
      try (UncheckedStatement statement = uncheckedConnection.createStatement(1000)) {
//        statement.execute("PRAGMA journal_mode = OFF");
        statement.execute("PRAGMA synchronous = 0");
        statement.execute("PRAGMA cache_size=1000000");
        statement.execute("PRAGMA locking_mode=EXCLUSIVE");
        statement.execute("PRAGMA temp_store=MEMORY");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      return uncheckedConnection;
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }
  }
}
