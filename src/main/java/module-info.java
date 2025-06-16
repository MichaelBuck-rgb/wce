module com.powergem.wce.main {
  requires com.fasterxml.jackson.databind;
  requires info.picocli;
  requires java.sql;
  requires java.desktop;
  requires org.xerial.sqlitejdbc;

  opens com.powergem.wce.commands to info.picocli;

  exports com.powergem.worstcasetrlim.model to com.fasterxml.jackson.databind;
  exports com.powergem.worstcasetrlim.normalized.model to com.fasterxml.jackson.databind;
}