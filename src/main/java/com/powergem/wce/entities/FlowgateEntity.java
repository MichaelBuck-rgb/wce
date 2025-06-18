package com.powergem.wce.entities;

public record FlowgateEntity(int scenarioId, int id, int busId, double dfax, double trlim, String mon, String con, double rating, double loadingBefore, int equipment_index) {
}
