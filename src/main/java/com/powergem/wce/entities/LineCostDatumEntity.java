package com.powergem.wce.entities;

public record LineCostDatumEntity(int scenarioId, int id, double length, double maxRatingPerLine, double maxAllowedFlowPerLine, double upgradeCost, double newLineCost) {
}
