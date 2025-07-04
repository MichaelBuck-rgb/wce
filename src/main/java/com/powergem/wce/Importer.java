package com.powergem.wce;

import com.powergem.sql.UncheckedSQLException;
import com.powergem.worstcasetrlim.Utilities;
import com.powergem.worstcasetrlim.model.*;

import java.nio.file.Path;
import java.sql.*;
import java.util.Collection;
import java.util.List;

public final class Importer {
  private static final String BUSES_CREATE_TABLE = "CREATE TABLE buses (scenarioId INTEGER, id INTEGER, busnum INTEGER, busname TEXT, busvolt REAL, busarea TEXT, trlim REAL, lat REAL, lon REAL)";
  private static final String BUSES_INSERT_STATEMENT_TEMPLATE = "INSERT INTO buses VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";

  private static final String STRESSGENS_CREATE_TABLE = "CREATE TABLE stressgens (scenarioId INTEGER, id INTEGER, busnum INTEGER, busname TEXT, busvolt REAL, busarea TEXT, lat REAL, lon REAL)";
  private static final String STRESSGENS_INSERT_STATEMENT_TEMPLATE = "INSERT INTO stressgens VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

  private static final String BRANCHTERMINALS_CREATE_TABLE = "CREATE TABLE branchterminals (scenarioId INTEGER, id INTEGER, name TEXT, kv REAL, areanum INTEGER, areaname TEXT, lat REAL, lon REAL)";
  private static final String BRANCHTERMINALS_INSERT_STATEMENT_TEMPLATE = "INSERT INTO branchterminals VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

  private static final String FLOWGATES_CREATE_TABLE = "CREATE TABLE flowgates (scenarioId INTEGER, id INTEGER, busid INTEGER, dfax REAL, trlim REAL, mon TEXT, con TEXT, rating REAL, loadingbefore REAL, equipment_index INTEGER)";
  private static final String FLOWGATES_INSERT_STATEMENT_TEMPLATE = "INSERT INTO flowgates VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  private static final String HARMERS_CREATE_TABLE = "CREATE TABLE harmers (scenarioId INTEGER, id INTEGER, flowgateId INTEGER, stressGenId INTEGER, dfax REAL, mwchange REAL, mwimpact REAL, pmax REAL, pgen REAL)";
  private static final String HARMERS_INSERT_STATEMENT_TEMPLATE = "INSERT INTO harmers VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";

  private static final String CONSTRAINTS_CREATE_TABLE = "CREATE TABLE constraints (scenarioId INTEGER, flowgateid INTEGER, montype INTEGER, frbus INTEGER, tobus INTEGER)";
  private static final String CONSTRAINTS_INSERT_STATEMENT_TEMPLATE = "INSERT INTO constraints VALUES(?, ?, ?, ?, ?)";

  private static final String LINE_COST_DATA_CREATE_TABLE = "CREATE TABLE line_cost_data (scenarioId INTEGER, id INTEGER, length REAL, max_rating_per_line REAL, max_allowed_flow_per_line REAL, upgrade_cost REAL, new_line_cost REAL)";
//  private static final String CONSTRAINTS_INSERT_STATEMENT_TEMPLATE = "INSERT INTO constraints VALUES(?, ?, ?, ?, ?)";

  private Importer() {
  }

  public static void importData(Path file, Connection connection) {
    WorstCaseTrLim worstCaseTrLim = Utilities.getWorstCaseTrLim(file);

    worstCaseTrLim = decrypt(worstCaseTrLim);

    try {
      try (Statement statement = connection.createStatement()) {
        statement.execute("PRAGMA synchronous = OFF");
        statement.execute("PRAGMA journal_mode = MEMORY");
        statement.execute("PRAGMA page_size = 1024");
      }

      createScenariosTable(worstCaseTrLim.wcResults(), connection);

      try (Statement statement = connection.createStatement()) {
        statement.execute(BUSES_CREATE_TABLE);
        statement.execute(STRESSGENS_CREATE_TABLE);
        statement.execute(BRANCHTERMINALS_CREATE_TABLE);
        statement.execute(FLOWGATES_CREATE_TABLE);
        statement.execute(HARMERS_CREATE_TABLE);
        statement.execute(CONSTRAINTS_CREATE_TABLE);
        statement.execute(LINE_COST_DATA_CREATE_TABLE);
      }

      for (WcResult wcResult : worstCaseTrLim.wcResults()) {
        int id = Integer.parseInt(wcResult.id());
        createBusTable(wcResult.buses(), id, connection);
        createStressGensTable(wcResult.StressGens(), id, connection);
        createBranchTerminalsTable(wcResult.branchTerminalList(), id, connection);
        createFlowgatesTable(wcResult.flowgates(), id, connection);
        createHarmersTable(wcResult.flowgates(), id, connection);
        createConstraintsTable(wcResult.flowgates(), id, connection);
        createLineCostDataTable(wcResult.lineCostData(), id, connection);
      }

      // create indices
      try (Statement statement = connection.createStatement()) {
        statement.execute("CREATE INDEX bus_idx ON buses (scenarioId, id)");
        statement.execute("CREATE INDEX busnum_idx ON buses (scenarioId, busnum)");
        statement.execute("CREATE INDEX stressgens_idx ON stressgens (scenarioId, id)");
        statement.execute("CREATE INDEX branchterminals_idx ON branchterminals (scenarioId, id)");
        statement.execute("CREATE INDEX flowgates_idx ON flowgates (scenarioId, id, busid)");
        statement.execute("CREATE INDEX harmers_idx ON harmers (scenarioId, id, flowgateId)");
        statement.execute("CREATE INDEX constraints_idx ON constraints (scenarioId, flowgateId)");
        statement.execute("CREATE INDEX line_cost_data_idx ON line_cost_data (scenarioId, id)");
      }

    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private static void createLineCostDataTable(List<LineCostData> lineCostData, int scenarioId, Connection connection) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement("INSERT INTO line_cost_data VALUES(?, ?, ?, ?, ?, ?, ?)")) {
      lineCostData.forEach(lineCostDatum -> {
        try {
          statement.setInt(1, scenarioId);
          statement.setInt(2, lineCostDatum.id());
          statement.setFloat(3, lineCostDatum.length());
          statement.setFloat(4, lineCostDatum.maxRatingPerLine());
          statement.setFloat(5, lineCostDatum.maxAllowedFlowPerLine());
          statement.setFloat(6, lineCostDatum.upgradeCost());
          statement.setFloat(7, lineCostDatum.newLineCost());
          statement.addBatch();
        } catch (SQLException e) {
          throw new UncheckedSQLException(e);
        }
      });
      statement.executeBatch();
    }
  }

  private static void createScenariosTable(List<WcResult> wcResults, Connection connection) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute("CREATE TABLE scenarios (id TEXT, version TEXT, name TEXT, mode TEXT)");
    }

    try (PreparedStatement statement = connection.prepareStatement("INSERT INTO scenarios VALUES(?, ?, ?, ?)")) {
      for (WcResult wcResult : wcResults) {
        statement.setString(1, wcResult.id());

        String version = wcResult.version();
        if (version == null || version.isEmpty()) {
          version = "1.0";
        }

        statement.setString(2, version);
        statement.setString(3, wcResult.title());
        // todo: determine mode if not found or empty
        statement.setString(4, "injection");
        statement.addBatch();
      }
      statement.executeBatch();
    }
  }

  private static void createConstraintsTable(List<Flowgate> flowgates, int scenarioId, Connection connection) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(CONSTRAINTS_INSERT_STATEMENT_TEMPLATE)) {
      for (Flowgate flowgate : flowgates) {
        int[] monTypes = flowgate.monType();
        int[] frBus = flowgate.frBuses();
        int[] toBus = flowgate.toBuses();

        for (int i = 0; i < monTypes.length; ++i) {
          statement.setInt(1, scenarioId);
          statement.setInt(2, flowgate.id());
          statement.setInt(3, monTypes[i]);
          statement.setInt(4, frBus[i]);
          statement.setInt(5, toBus[i]);
          statement.addBatch();
        }
        statement.executeBatch();
      }
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
            decryptBranchTerminals(wcResult.branchTerminalList()),
            wcResult.type(),
            wcResult.lineCostData(),
            wcResult.transformerCostData()
    );
  }

  private static List<BranchTerminal> decryptBranchTerminals(List<BranchTerminal> branchTerminals) {
    return branchTerminals.stream().map(bt -> new BranchTerminal(bt.id(), bt.name(), bt.kv(), bt.areanum(), bt.areaname(), decryptLat(bt.lat(), bt.lon()), decryptLon(bt.lat(), bt.lon()))).toList();
  }

  private static List<StressGen> decryptStressGens(List<StressGen> stressGens) {
    return stressGens.stream().map(stressGen -> new StressGen(stressGen.id(), stressGen.busnum(), stressGen.busname(), stressGen.busvolt(), stressGen.busarea(), decryptLat(stressGen.lat(), stressGen.lon()), decryptLon(stressGen.lat(), stressGen.lon()))).toList();
  }

  private static List<Bus> decryptBuses(List<Bus> buses) {
    return buses.stream().map(bus -> new Bus(bus.id(), bus.busnum(), bus.busname(), bus.busvolt(), bus.busarea(), bus.trlim(), decryptLat(bus.lat(), bus.lon()), decryptLon(bus.lat(), bus.lon()))).toList();
  }

  private static void createBusTable(Collection<Bus> buses, int scenarioId, Connection connection) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(BUSES_INSERT_STATEMENT_TEMPLATE)) {
      for (Bus bus : buses) {
        int index = 1;
        statement.setInt(index++, scenarioId);
        statement.setInt(index++, bus.id());
        statement.setInt(index++, bus.busnum());
        statement.setString(index++, bus.busname());
        statement.setDouble(index++, bus.busvolt());
        statement.setString(index++, bus.busarea());
        statement.setDouble(index++, bus.trlim());
        statement.setDouble(index++, bus.lat());
        statement.setDouble(index, bus.lon());
        statement.addBatch();
      }
      statement.executeBatch();
    }
  }

  private static void createStressGensTable(Collection<StressGen> stressGens, int scenarioId, Connection connection) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(STRESSGENS_INSERT_STATEMENT_TEMPLATE)) {
      for (StressGen stressGen : stressGens) {
        int index = 1;
        statement.setInt(index++, scenarioId);
        statement.setInt(index++, stressGen.id());
        statement.setInt(index++, stressGen.busnum());
        statement.setString(index++, stressGen.busname());
        statement.setDouble(index++, stressGen.busvolt());
        statement.setString(index++, stressGen.busarea());
        statement.setDouble(index++, stressGen.lat());
        statement.setDouble(index, stressGen.lon());
        statement.addBatch();
      }
      statement.executeBatch();
    }
  }

  private static void createBranchTerminalsTable(Collection<BranchTerminal> branchTerminals, int scenarioId, Connection connection) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(BRANCHTERMINALS_INSERT_STATEMENT_TEMPLATE)) {
      for (BranchTerminal branchTerminal : branchTerminals) {
        int index = 1;
        statement.setInt(index++, scenarioId);
        statement.setInt(index++, branchTerminal.id());
        statement.setString(index++, branchTerminal.name());
        statement.setDouble(index++, branchTerminal.kv());
        statement.setInt(index++, branchTerminal.areanum());
        statement.setString(index++, branchTerminal.areaname());
        statement.setDouble(index++, branchTerminal.lat());
        statement.setDouble(index, branchTerminal.lon());
        statement.addBatch();
      }
      statement.executeBatch();
    }
  }

  private static void createFlowgatesTable(Collection<Flowgate> flowgates, int scenarioId, Connection connection) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(FLOWGATES_INSERT_STATEMENT_TEMPLATE)) {
      for (Flowgate flowgate : flowgates) {

        int[] equipmentIndices = flowgate.equipmentIndex();
        if (equipmentIndices.length > 0) {
          for (int equipmentIndex : equipmentIndices) {
            int index = 1;
            statement.setInt(index++, scenarioId);
            statement.setInt(index++, flowgate.id());
            statement.setInt(index++, flowgate.busid());
            statement.setDouble(index++, flowgate.dfax());
            statement.setDouble(index++, flowgate.trlim());
            statement.setString(index++, flowgate.mon());
            statement.setString(index++, flowgate.con());
            statement.setDouble(index++, flowgate.rating());
            statement.setDouble(index++, flowgate.loadingbefore());
            statement.setInt(index++, equipmentIndex);
          }
        } else {
          int index = 1;
          statement.setInt(index++, scenarioId);
          statement.setInt(index++, flowgate.id());
          statement.setInt(index++, flowgate.busid());
          statement.setDouble(index++, flowgate.dfax());
          statement.setDouble(index++, flowgate.trlim());
          statement.setString(index++, flowgate.mon());
          statement.setString(index++, flowgate.con());
          statement.setDouble(index++, flowgate.rating());
          statement.setDouble(index++, flowgate.loadingbefore());
          statement.setNull(index++, Types.INTEGER);
        }
        statement.addBatch();
      }
      statement.executeBatch();
    }
  }

  private static void createHarmersTable(List<Flowgate> flowgates, int scenarioId, Connection connection) throws SQLException {
    int harmerId = 0;
    try (PreparedStatement statement = connection.prepareStatement(HARMERS_INSERT_STATEMENT_TEMPLATE)) {
      for (Flowgate flowgate : flowgates) {
        List<Harmer> harmers = flowgate.harmers();
        for (Harmer harmer : harmers) {
          int index = 1;
          statement.setInt(index++, scenarioId);
          statement.setInt(index++, harmerId);
          statement.setInt(index++, flowgate.id());
          statement.setInt(index++, harmer.index());
          statement.setDouble(index++, harmer.dfax());
          statement.setDouble(index++, harmer.mwchange());
          statement.setDouble(index++, harmer.mwimpact());
          statement.setDouble(index++, harmer.pmax());
          statement.setDouble(index, harmer.pgen());
          statement.addBatch();
          ++harmerId;
        }
        statement.executeBatch();
      }
    }
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
