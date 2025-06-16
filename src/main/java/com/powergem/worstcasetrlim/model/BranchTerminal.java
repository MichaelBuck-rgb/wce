package com.powergem.worstcasetrlim.model;

public record BranchTerminal(int id, String name, double kv, int areaNum, String areaname, double lat, double lon, int areanum) implements ILocation {
}
