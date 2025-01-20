package com.powergem.wce.commands;

import com.powergem.wce.Importer;
import com.powergem.wce.Utils;
import com.powergem.wce.entities.HarmerEntity;
import com.powergem.worstcasetrlim.model.Flowgate;
import com.powergem.worstcasetrlim.model.Harmer;
import com.powergem.worstcasetrlim.model.StressGen;
import picocli.CommandLine;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.Callable;

import static com.powergem.wce.Utils.getConnection;

@CommandLine.Command(name = "harmers")
public final class ListHarmersCommand implements Callable<Integer> {
  @CommandLine.Parameters(index = "1", description = "The ID of the flowgate to get the harmers of.")
  private int flowgateid;

  @CommandLine.Parameters(index = "0", description = "The JSON file to inspect.")
  private Path jsonFile;

  @Override
  public Integer call() throws Exception {
    String jdbcUrl = "jdbc:sqlite::memory:";

    try (Connection connection = getConnection(jdbcUrl)) {
      Importer.importData(this.jsonFile, connection);

      Optional<Flowgate> optionalFlowgate = getFlowgate(this.flowgateid, connection);
      if (optionalFlowgate.isEmpty()) {
        System.err.println("Flowgate " + this.flowgateid + " not found.");
        return -1;
      }

      System.out.printf("Harmers for flowgate %s%n", optionalFlowgate.get());

      try (PreparedStatement statement = connection.prepareStatement("select * from harmers where flowgateId = ?")) {
        statement.setInt(1, this.flowgateid);
        try (ResultSet resultSet = statement.executeQuery()) {
          int id = 1;
          while (resultSet.next()) {
            HarmerEntity harmerEntity = toHarmerEntity(resultSet);
            Harmer harmer = new Harmer(String.valueOf(id), harmerEntity.stressGenId(), harmerEntity.dfax(), harmerEntity.mwchange(), harmerEntity.mwimpact(), harmerEntity.pmax(), harmerEntity.pgen());
            Optional<StressGen> stressGen = getStressGen(harmerEntity.stressGenId(), connection);
            System.out.printf("%s%n", harmer);
            stressGen.ifPresent(gen -> System.out.printf("\t%s%n", gen));
            ++id;
          }
        }
      }
    }
    return 0;
  }

  private Optional<Flowgate> getFlowgate(int id, Connection connection) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement("select * from flowgates where id = ?")) {
      statement.setInt(1, id);
      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          return Optional.of(Utils.toFlowgate(rs));
        }
      }
    }
    return Optional.empty();
  }

  private Optional<StressGen> getStressGen(int id, Connection connection) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement("select * from stressgens where id = ?")) {
      statement.setInt(1, id);
      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          return Optional.of(new StressGen(resultSet.getInt(1), resultSet.getInt("busnum"), resultSet.getString("busname"), resultSet.getDouble("busvolt"), resultSet.getString("busarea"), resultSet.getDouble("lat"), resultSet.getDouble("lon")));
        }
      }
    }

    return Optional.empty();
  }

  private HarmerEntity toHarmerEntity(ResultSet resultSet) throws SQLException {
    return new HarmerEntity(
            resultSet.getInt("id"),
            resultSet.getInt("flowgateId"),
            resultSet.getInt("stressGenId"),
            resultSet.getDouble("dfax"),
            resultSet.getDouble("mwchange"),
            resultSet.getDouble("mwimpact"),
            resultSet.getDouble("pmax"),
            resultSet.getDouble("pgen")
    );
  }
}
