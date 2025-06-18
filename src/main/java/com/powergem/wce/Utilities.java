package com.powergem.wce;

import com.powergem.MonType;
import com.powergem.wce.entities.*;
import com.powergem.worstcasetrlim.model.BranchTerminal;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Utilities {

  private Utilities() {
  }

  public static void dumpFlowgate(FlowgateEntity flowgate, DataFile dataFile, int scenarioId, int indent) {
    String strIndent = " ".repeat(indent);

    System.out.printf("%n%s[Flowgate] [%s]%n", strIndent, toString(flowgate));
    List<ConstraintsEntity> constraints = dataFile.getConstraints(scenarioId, flowgate.id());
    constraints.forEach(constraint -> MonType.getMonType(constraint.monType()).ifPresent(monType -> {
      if (monType == MonType.LINE) {
        BranchTerminal from = dataFile.getBranchBus(scenarioId, constraint.frBus()).orElseThrow();
        BranchTerminal to = dataFile.getBranchBus(scenarioId, constraint.toBus()).orElseThrow();
        System.out.printf("%n%s  [Line Constraint]%n", strIndent);
        System.out.printf("%s    [From] [%s]%n", strIndent, toString(from));
        System.out.printf("%s      [To] [%s]%n", strIndent, toString(to));

        LineCostDatumEntity lineCostDatum = dataFile.getLineCostDatumById(flowgate.equipment_index(), scenarioId).orElseThrow();
        System.out.printf("%s    [Cost] [%s]%n", strIndent, toString(lineCostDatum));
      } else if (monType == MonType.TRANSFORMER) {
        BranchTerminal from = dataFile.getBranchBus(scenarioId, constraint.frBus()).orElseThrow();
        System.out.printf("%s  [Transformer Constraint]%n", strIndent);
        System.out.printf("%s    [%s]%n", strIndent, toString(from));
      } else {
        System.out.printf("%s  [Unknown Constraint]", strIndent);
      }
    }));

    System.out.printf("%n%s  [Harmers]%n", strIndent);
    dataFile.getHarmers(scenarioId, flowgate.id()).forEach(harmer -> System.out.printf("%s    %s]%n", strIndent, toString(harmer)));
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
    String percentLoad = "%.2f".formatted(flowgate.loadingBefore());
    if (flowgate.loadingBefore() >= 100) {
      percentLoad = "\u001B[31m" + percentLoad + "\u001B[0m";
    }

    LinkedHashMap<String, String> map = new LinkedHashMap<>(6);
    map.put("id", String.valueOf(flowgate.id()));
    map.put("dfax", String.valueOf(flowgate.dfax()));
    map.put("trlim", String.valueOf(flowgate.trlim()));
    map.put("mon", "'" + flowgate.mon().trim() + "'");
    map.put("con", "'" + flowgate.con().trim() + "'");
    map.put("rating", String.format("%.2f", flowgate.rating()));
    map.put("%load", percentLoad);

    return toString(map);
  }

  public static String toString(HarmerEntity harmer) {
    LinkedHashMap<String, String> map = new LinkedHashMap<>(6);
    map.put("id", String.valueOf(harmer.id()));
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

  public static String toString(BusEntity bus) {
    Map<String, String> map = new LinkedHashMap<>();

    map.put("id", String.valueOf(bus.id()));
    map.put("busnum", String.valueOf(bus.busnum()));
    map.put("busname", "'" + bus.busname() + "'");
    map.put("busvolt", String.format("%.2f", bus.busvolt()));
    map.put("busarea", "'" + bus.busarea() + "'");
    map.put("trlim", String.valueOf(bus.trlim()));
    map.put("location", toString(bus.lat(), bus.lon()));

    return toString(map);
  }
}
