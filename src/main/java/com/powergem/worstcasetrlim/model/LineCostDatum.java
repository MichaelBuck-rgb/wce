package com.powergem.worstcasetrlim.model;

public record LineCostDatum(int id, float length, float maxRatingPerLine, float maxAllowedFlowPerLine, float upgradeCost, float newLineCost) {
}
