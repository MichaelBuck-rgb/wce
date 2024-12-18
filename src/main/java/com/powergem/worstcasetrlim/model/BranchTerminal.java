package com.powergem.worstcasetrlim.model;

public record BranchTerminal(int id, String name, double kv, int areaNum, String areaName, double lat, double lon) implements ILocation {
}
