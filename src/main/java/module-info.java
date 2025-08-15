module com.powergem.wce.main {
  requires info.picocli;
  requires java.sql;
  requires java.desktop;
  requires org.xerial.sqlitejdbc;
  requires java.compiler;

  opens com.powergem.wce.commands to info.picocli;
  opens com.powergem.wce.mapstruct to org.mapstruct;

  requires io.avaje.jsonb;
  requires org.mapstruct;
  requires io.soabase.recordbuilder.core;

  // you must define the fully qualified class name of the generated classes. if you use an import statement, compilation will fail
  provides io.avaje.jsonb.spi.JsonbExtension with com.powergem.worstcasetrlim.model.jsonb.GeneratedJsonComponent;
}