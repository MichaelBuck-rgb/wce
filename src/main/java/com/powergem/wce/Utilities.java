package com.powergem.wce;

import com.powergem.MonType;
import com.powergem.wce.entities.*;
import com.powergem.worstcasetrlim.model.BranchTerminal;

import java.util.*;
import java.util.stream.Collectors;

public final class Utilities {

  private Utilities() {
  }

  public static void dumpFlowgate(FlowgateEntity flowgate, DataFile dataFile, int scenarioId, int indent, Set<ReportType> exclusions) {
    String strIndent = " ".repeat(indent);

    System.out.printf("%n%s[Flowgate] [%s]%n", strIndent, toString(flowgate));
    List<ConstraintsEntity> constraints = dataFile.getConstraints(scenarioId, flowgate.id());
    constraints.forEach(constraint -> MonType.getMonType(constraint.monType()).ifPresent(monType -> {
      if (monType == MonType.LINE) {
        if (!exclusions.contains(ReportType.LINE_CONSTRAINTS)) {
          BranchTerminal from = dataFile.getBranchBus(scenarioId, constraint.frBus()).orElseThrow();
          BranchTerminal to = dataFile.getBranchBus(scenarioId, constraint.toBus()).orElseThrow();
          System.out.printf("%n%s  [Line Constraint]%n", strIndent);
          System.out.printf("%s    [From] [%s]%n", strIndent, toString(from));
          System.out.printf("%s      [To] [%s]%n", strIndent, toString(to));

          LineCostDatumEntity lineCostDatum = dataFile.getLineCostDatumById(flowgate.equipment_index().orElseThrow(), scenarioId).orElseThrow();
          System.out.printf("%s    [Cost] [%s]%n", strIndent, toString(lineCostDatum));
        }
      } else if (monType == MonType.TRANSFORMER) {
        BranchTerminal from = dataFile.getBranchBus(scenarioId, constraint.frBus()).orElseThrow();
        System.out.printf("%s  [Transformer Constraint]%n", strIndent);
        System.out.printf("%s    [%s]%n", strIndent, toString(from));
      } else {
        System.out.printf("%s  [Unknown Constraint]", strIndent);
      }
    }));

    if (!exclusions.contains(ReportType.HARMERS)) {
      System.out.printf("%n%s  [Harmers]%n", strIndent);
      dataFile.getHarmers(scenarioId, flowgate.id()).forEach(harmer -> System.out.printf("%s    %s]%n", strIndent, toString(harmer)));
    }
  }

  private static String toString(LineCostDatumEntity lineCostDatum) {
    String strLength = lengthToString(lineCostDatum.length());

    Map<String, String> map = new LinkedHashMap<>(6);

    map.put("id", String.valueOf(lineCostDatum.id()));
    map.put("length", strLength);
    map.put("maxRatingPerLine", String.valueOf(lineCostDatum.maxRatingPerLine()));
    map.put("maxAllowedFlowPerLine", String.valueOf(lineCostDatum.maxAllowedFlowPerLine()));
    map.put("upgradeCost", String.valueOf(lineCostDatum.upgradeCost()));
    map.put("newLineCost", String.valueOf(lineCostDatum.newLineCost()));

    return toString(map);
  }

  private static String lengthToString(double length) {
    String strLength = String.valueOf(length);
    if (length > 0) {
      return strLength;
    }

    return red(strLength);
  }

  public static String toString(FlowgateEntity flowgate) {
    String percentLoad = "%.2f".formatted(flowgate.loadingbefore());
    if (flowgate.loadingbefore() >= 100) {
      percentLoad = "\u001B[31m" + percentLoad + "\u001B[0m";
    }

    LinkedHashMap<String, String> map = new LinkedHashMap<>(6);
    map.put("id", String.valueOf(flowgate.id()));
    map.put("dfax", String.valueOf(flowgate.dfax()));
    map.put("trlim", String.valueOf(flowgate.trlim()));
    map.put("mon", "'" + flowgate.mon() + "'");
    map.put("con", "'" + flowgate.con() + "'");
    map.put("rating", String.format("%.2f", flowgate.rating()));
    map.put("%load", percentLoad);

    return toString(map);
  }

  public static String toString(HarmerEntity harmer) {
    LinkedHashMap<String, String> map = new LinkedHashMap<>(6);
    map.put("id", String.valueOf(harmer.id()));
    map.put("index", String.valueOf(harmer.index()));
    map.put("dfax", String.valueOf(harmer.dfax()));
    map.put("MW Change", String.valueOf(harmer.mwchange()));
    map.put("MW Impact", String.valueOf(harmer.mwimpact()));
    map.put("pmax", String.valueOf(harmer.pmax()));
    map.put("pgen", String.valueOf(harmer.pgen()));

    return toString(map);
  }

  public static String toString(BranchTerminal branchTerminal) {
    String location = toString(branchTerminal.lat(), branchTerminal.lon());
    LinkedHashMap<String, String> map = new LinkedHashMap<>(6);

    map.put("id", String.valueOf(branchTerminal.id()));
    map.put("name", "'" + branchTerminal.name() + "'");
    map.put("kv", String.format("%.2f", branchTerminal.kv()));
    map.put("areanum", String.valueOf(branchTerminal.areanum()));
    map.put("areaname", "'" + branchTerminal.areaname() + "'");
    map.put("Location", location);

    return toString(map);
  }

  public static String toString(double lat, double lon) {
    if (lat == 0 && lon == 0) {
      return "(" + red(String.valueOf(lat)) + ", " + red(String.valueOf(lon)) + ")";
    }
    return "(%f, %f)".formatted(lat, lon);
  }

  public static String red(String s) {
    if (Boolean.getBoolean("wce.useAnsi")) {
      return "\u001B[31m" + s + "\u001B[0m";
    }
    return s;
  }

  public static String bold(String s) {
    if (Boolean.getBoolean("wce.useAnsi")) {
      return "\u001B[1m" + s + "\u001B[22m";
    }
    return s;
  }

  public static String toString(Map<String, String> m) {
    return m.entrySet().stream()
            .map(entry -> bold(entry.getKey()) + ": " + entry.getValue())
            .collect(Collectors.joining(", "));
  }

  public static String[] busHeader() {
    return new String[]{
            "Scenario",
            "id",
            "busnum",
            "busname",
            "busvolt",
            "busarea",
            "trlim",
            "location"};
  }

  public static String toString(BusEntity bus) {
    return String.join(", ", List.of(
            String.valueOf(bus.id()),
            String.valueOf(bus.busnum()),
            "\"" + bus.busname() + "\"",
            String.format("%.2f", bus.busvolt()),
            "\"" + bus.busarea() + "\"",
            String.valueOf(bus.trlim()),
            toString(bus.lat(), bus.lon())
    ));
  }

  public static Set<ReportType> toExclusions(String exclude) {
    return Arrays.stream(exclude.split(","))
            .map(String::trim)
            .map(String::toLowerCase)
            .map(Utilities::getReportType)
            .collect(Collectors.toSet());
  }

  private static ReportType getReportType(String s) {
    return switch (s.toLowerCase()) {
      case "harmers" -> ReportType.HARMERS;
      case "lineconstraints" -> ReportType.LINE_CONSTRAINTS;
      default -> throw new IllegalArgumentException("Unknown report type: " + s);
    };
  }

  static double encryptLat(double lat, double lon) {
    return Math.pow(lon - lat, 3);
  }

  static double encryptLon(double lat, double lon) {
    return Math.pow(lon + lat, 3);
  }

  static double decryptLat(double encryptedLat, double encryptedLon) {
    return (Math.signum(encryptedLon) * decrypt(encryptedLon) - Math.signum(encryptedLat) * decrypt(encryptedLat)) / 2;
  }

  static double decryptLon(double encryptedLat, double encryptedLon) {
    return (Math.signum(encryptedLon) * decrypt(encryptedLon) + Math.signum(encryptedLat) * decrypt(encryptedLat)) / 2;
  }

  private static double decrypt(double encrypted) {
    return Math.cbrt(Math.abs(encrypted));
  }
}
