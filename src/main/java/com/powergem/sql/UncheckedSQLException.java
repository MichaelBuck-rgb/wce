package com.powergem.sql;

import java.sql.SQLException;
import java.util.Objects;

public final class UncheckedSQLException extends RuntimeException {

  public UncheckedSQLException(String message, SQLException cause) {
    super(message, Objects.requireNonNull(cause));
  }

  public UncheckedSQLException(SQLException cause) {
    super(Objects.requireNonNull(cause));
  }

  @Override
  public SQLException getCause() {
    return (SQLException) super.getCause();
  }

}
