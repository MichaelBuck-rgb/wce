package com.powergem.worstcasetrlim.model;

public record LineCostData(int id, float length, float maxRatingPerLine, float maxAllowedFlowPerLine, float upgradeCost, float newLineCost) {
}
