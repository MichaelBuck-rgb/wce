package com.powergem.wce;

import com.powergem.worstcasetrlim.model.Flowgate;
import com.powergem.worstcasetrlim.model.Harmer;

import java.sql.*;
import java.util.List;
import java.util.Properties;

import static java.util.Collections.emptyList;

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

  public static Flowgate toFlowgate(ResultSet rs) throws SQLException {
    int id = rs.getInt("id");
    int busid = rs.getInt("busid");
    double dfax = rs.getDouble("dfax");
    double trlim = rs.getDouble("trlim");
    String mon = rs.getString("mon");
    String con = rs.getString("con");
    double rating = rs.getDouble("rating");
    double loadingbefore = rs.getDouble("loadingbefore");
    double loadingafter = rs.getDouble("loadingafter");
    double mwimpact = rs.getDouble("mwimpact");

    List<Harmer> harmers = emptyList();
    int[] frBuses = new int[0];
    int[] toBuses = new int[0];
    int[] monType = new int[0];

    return new Flowgate(id, busid, dfax, trlim, mon, con, rating, loadingbefore, loadingafter, mwimpact, harmers, frBuses, toBuses, monType);
  }
}
