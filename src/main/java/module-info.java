module com.powergem.wce.main {
  requires com.fasterxml.jackson.databind;
  requires info.picocli;
  requires java.sql;

  opens com.powergem.wce.commands to info.picocli;

  exports com.powergem.worstcasetrlim.model to com.fasterxml.jackson.databind;
}