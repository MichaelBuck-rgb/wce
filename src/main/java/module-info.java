module com.powergem.wce.main {
  requires info.picocli;
  requires java.sql;
  requires java.desktop;
  requires org.xerial.sqlitejdbc;

  opens com.powergem.wce.commands to info.picocli;

  requires io.avaje.jsonb;

  // you must define the fully qualified class name of the generated classes. if you use an import statement, compilation will fail
  provides io.avaje.jsonb.spi.JsonbExtension with com.powergem.worstcasetrlim.model.jsonb.GeneratedJsonComponent;
}