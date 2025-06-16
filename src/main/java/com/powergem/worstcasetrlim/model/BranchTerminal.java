package com.powergem.worstcasetrlim.model;

public record BranchTerminal(int id, String name, double kv, int areanum, String areaname, double lat, double lon) implements ILocation {
}
