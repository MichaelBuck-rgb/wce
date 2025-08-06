package com.powergem.wce.entities;

import java.util.Optional;

public record FlowgateEntity(int scenarioId, int id, int busId, double dfax, double trlim, String mon, String con, double rating, double loadingBefore, Optional<Integer> equipment_index) {
}
