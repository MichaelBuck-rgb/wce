package com.powergem.wce;

import com.powergem.worstcasetrlim.Utilities;
import com.powergem.worstcasetrlim.model.*;

import java.nio.file.Path;
import java.sql.*;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class Importer {
  private static final String BUSES_CREATE_TABLE = "CREATE TABLE buses (id INTEGER, busnum INTEGER, busname TEXT, busvolt REAL, busarea TEXT, trlim REAL, lat REAL, lon REAL)";
  private static final String BUSES_INSERT_STATEMENT_TEMPLATE = "INSERT INTO buses VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

  private static final String STRESSGENS_CREATE_TABLE = "CREATE TABLE stressgens (id INTEGER, busnum INTEGER, busvolt REAL, busarea TEXT, lat REAL, lon REAL)";
  private static final String STRESSGENS_INSERT_STATEMENT_TEMPLATE = "INSERT INTO stressgens VALUES(?, ?, ?, ?, ?, ?)";

  private static final String BRANCHTERMINAL_CREATE_TABLE = "CREATE TABLE branchterminals (id INTEGER, name TEXT, kv REAL, areanum INTEGER, areaname TEXT, lat REAL, lon REAL)";
  private static final String BRANCHTERMINAL_INSERT_STATEMENT_TEMPLATE = "INSERT INTO branchterminals VALUES(?, ?, ?, ?, ?, ?, ?)";

  private static final String FLOWGATE_CREATE_TABLE = "CREATE TABLE flowgates (id INTEGER, busid INTEGER, dfax REAL, trlim REAL, mon TEXT, con TEXT, rating REAL, loadingbefore REAL, loadingafter REAL, mwimpact REAL, frbuses TEXT, tobuses TEXT, montype TEXT)";

  private static final String HARMERS_CREATE_TABLE = "CREATE TABLE harmers (id TEXT, idx INTEGER, dfax REAL, mwchange REAL, mwimpact REAL, pmax REAL, pgen REAL, flowgateid INTEGER)";
  private static final String HARMERS_INSERT_STATEMENT_TEMPLATE = "INSERT INTO harmers VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

  private Importer() {
  }

  public static void importData(Path file, Connection connection) {
    WorstCaseTrLim worstCaseTrLim = Utilities.getWorstCaseTrLim(file);

    worstCaseTrLim = decrypt(worstCaseTrLim);

    try {
      try (Statement statement = connection.createStatement()) {
        statement.execute("PRAGMA synchronous = OFF");
        statement.execute("PRAGMA journal_mode = MEMORY");
      }
      WcResult wcResult = worstCaseTrLim.wcResults().getFirst();
      createBusTable(wcResult.buses(), connection);
      createStressGensTable(wcResult.StressGens(), connection);
      createBranchTerminalsTable(wcResult.branchTerminalList(), connection);
      createFlowgatesTable(wcResult.flowgates(), connection);
      createHarmersTable(wcResult.flowgates(), connection);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private static WorstCaseTrLim decrypt(WorstCaseTrLim worstCaseTrLim) {
    List<WcResult> list = worstCaseTrLim.wcResults().stream().map(Importer::decrypt).toList();
    return new WorstCaseTrLim(list);
  }

  private static WcResult decrypt(WcResult wcResult) {
    return new WcResult(
            wcResult.version(),
            wcResult.id(),
            wcResult.title(),
            decryptBuses(wcResult.buses()),
            decryptStressGens(wcResult.StressGens()),
            wcResult.flowgates(),
            decryptBranchTerminals(wcResult.branchTerminalList())
    );
  }

  private static List<BranchTerminal> decryptBranchTerminals(List<BranchTerminal> branchTerminals) {
    return branchTerminals.stream().map(bt -> new BranchTerminal(bt.id(), bt.name(), bt.kv(), bt.areaNum(), bt.areaName(), decryptLat(bt.lat(), bt.lon()), decryptLon(bt.lat(), bt.lon()))).toList();
  }

  private static List<StressGen> decryptStressGens(List<StressGen> stressGens) {
    return stressGens.stream().map(stressGen -> new StressGen(stressGen.id(), stressGen.busnum(), stressGen.busname(), stressGen.busvolt(), stressGen.busarea(), decryptLat(stressGen.lat(), stressGen.lon()), decryptLon(stressGen.lat(), stressGen.lon()))).toList();
  }

  private static List<Bus> decryptBuses(List<Bus> buses) {
    return buses.stream().map(bus -> new Bus(bus.id(), bus.busnum(), bus.busname(), bus.busvolt(), bus.busarea(), bus.trlim(), decryptLat(bus.lat(), bus.lon()), decryptLon(bus.lat(), bus.lon()))).toList();
  }

  private static void createBusTable(Collection<Bus> buses, Connection connection) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute(BUSES_CREATE_TABLE);
    }

    try (PreparedStatement statement = connection.prepareStatement(BUSES_INSERT_STATEMENT_TEMPLATE)) {
      for (Bus bus : buses) {
        statement.setInt(1, bus.id());
        statement.setInt(2, bus.busnum());
        statement.setString(3, bus.busname());
        statement.setDouble(4, bus.busvolt());
        statement.setString(5, bus.busarea());
        statement.setDouble(6, bus.trlim());
        statement.setDouble(7, bus.lat());
        statement.setDouble(8, bus.lon());
        statement.addBatch();
      }
      statement.executeBatch();
    }
  }

  private static void createStressGensTable(Collection<StressGen> stressGens, Connection connection) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute(STRESSGENS_CREATE_TABLE);
    }

    try (PreparedStatement statement = connection.prepareStatement(STRESSGENS_INSERT_STATEMENT_TEMPLATE)) {
      for (StressGen stressGen : stressGens) {
        statement.setInt(1, stressGen.id());
        statement.setInt(2, stressGen.busnum());
        statement.setDouble(3, stressGen.busvolt());
        statement.setString(4, stressGen.busarea());
        statement.setDouble(5, stressGen.lat());
        statement.setDouble(6, stressGen.lon());
        statement.addBatch();
      }
      statement.executeBatch();
    }
  }

  private static void createBranchTerminalsTable(Collection<BranchTerminal> branchTerminals, Connection connection) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute(BRANCHTERMINAL_CREATE_TABLE);
    }

    try (PreparedStatement statement = connection.prepareStatement(BRANCHTERMINAL_INSERT_STATEMENT_TEMPLATE)) {
      for (BranchTerminal branchTerminal : branchTerminals) {
        statement.setInt(1, branchTerminal.id());
        statement.setString(2, branchTerminal.name());
        statement.setDouble(3, branchTerminal.kv());
        statement.setInt(4, branchTerminal.areaNum());
        statement.setString(5, branchTerminal.areaName());
        statement.setDouble(6, branchTerminal.lat());
        statement.setDouble(7, branchTerminal.lon());
        statement.addBatch();
      }
      statement.executeBatch();
    }
  }

  private static void createFlowgatesTable(Collection<Flowgate> flowgates, Connection connection) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute(FLOWGATE_CREATE_TABLE);
    }

    for (Flowgate flowgate : flowgates) {
      int[] frBuses = flowgate.frBuses();
      String frBusesJsonArrayTemplate = toJsonArrayTemplate(frBuses.length);

      int[] toBuses = flowgate.toBuses();
      String toBusesJsonArrayTemplate = toJsonArrayTemplate(toBuses.length);

      int[] monType = flowgate.monType();
      String monTypeJsonArrayTemplate = toJsonArrayTemplate(monType.length);

      String statementTemplate = "INSERT INTO flowgates VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + frBusesJsonArrayTemplate + ", " + toBusesJsonArrayTemplate + ", " + monTypeJsonArrayTemplate + ")";

      try (PreparedStatement statement = connection.prepareStatement(statementTemplate)) {
        statement.setInt(1, flowgate.id());
        statement.setInt(2, flowgate.busid());
        statement.setDouble(3, flowgate.dfax());
        statement.setDouble(4, flowgate.trlim());
        statement.setString(5, flowgate.mon());
        statement.setString(6, flowgate.con());
        statement.setDouble(7, flowgate.rating());
        statement.setDouble(8, flowgate.loadingbefore());
        statement.setDouble(9, flowgate.loadingafter());
        statement.setDouble(10, flowgate.mwimpact());

        for (int i = 0; i < frBuses.length; ++i) {
          statement.setInt(10 + i + 1, frBuses[i]);
        }

        for (int i = 0; i < toBuses.length; ++i) {
          statement.setInt(10 + frBuses.length + i + 1, toBuses[i]);
        }

        for (int i = 0; i < monType.length; ++i) {
          statement.setInt(10 + frBuses.length + toBuses.length + i + 1, monType[i]);
        }

        statement.execute();
      } catch (SQLException e) {
        throw new RuntimeException("Exception while writing flowgate with id " + flowgate.id(), e);
      }
    }
  }

  private static void createHarmersTable(List<Flowgate> flowgates, Connection connection) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute(HARMERS_CREATE_TABLE);
    }

    try (PreparedStatement statement = connection.prepareStatement(HARMERS_INSERT_STATEMENT_TEMPLATE)) {
      for (Flowgate flowgate : flowgates) {
        List<Harmer> harmers = flowgate.harmers();
        for (Harmer harmer : harmers) {
          statement.setString(1, harmer.id());
          statement.setInt(2, harmer.index());
          statement.setDouble(3, harmer.dfax());
          statement.setDouble(4, harmer.mwchange());
          statement.setDouble(5, harmer.mwimpact());
          statement.setDouble(6, harmer.pmax());
          statement.setDouble(7, harmer.pgen());
          statement.setInt(8, flowgate.id());
          statement.addBatch();
        }
        statement.executeBatch();
      }
    }
  }

  private static String toJsonArrayTemplate(int length) {
    return IntStream.range(0, length)
            .mapToObj(value -> "?")
            .collect(Collectors.joining(",", "json_array(", ")"));
  }

  private static double decryptLat(double encryptedLat, double encryptedLon) {
    return (Math.signum(encryptedLon) * decrypt(encryptedLon) - Math.signum(encryptedLat) * decrypt(encryptedLat)) / 2;
  }

  private static double decryptLon(double encryptedLat, double encryptedLon) {
    return (Math.signum(encryptedLon) * decrypt(encryptedLon) + Math.signum(encryptedLat) * decrypt(encryptedLat)) / 2;
  }

  private static double decrypt(double encrypted) {
    return Math.cbrt(Math.abs(encrypted));
  }
}
