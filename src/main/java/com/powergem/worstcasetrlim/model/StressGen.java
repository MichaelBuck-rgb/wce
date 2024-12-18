package com.powergem.worstcasetrlim.model;

public record StressGen(int id, int busnum, String busname, double busvolt, String busarea, double lat, double lon) implements ILocation {
}
