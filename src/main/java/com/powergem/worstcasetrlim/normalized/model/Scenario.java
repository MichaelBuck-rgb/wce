package com.powergem.worstcasetrlim.normalized.model;

public record Scenario(String title, String version, String id, Table buses, Table flowgates, Table stressGens, Table branchTerminals) {
}
