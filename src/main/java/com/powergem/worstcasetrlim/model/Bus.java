package com.powergem.worstcasetrlim.model;

public record Bus(int id, int busnum, String busname, double busvolt, String busarea, double trlim, double lat, double lon) implements ILocation {
}
