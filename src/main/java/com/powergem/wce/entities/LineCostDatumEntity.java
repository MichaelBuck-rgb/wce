package com.powergem.wce.entities;

public record LineCostDatumEntity(int scenarioId, int id, float length, float maxRatingPerLine, float maxAllowedFlowPerLine, float upgradeCost, float newLineCost) {
}
