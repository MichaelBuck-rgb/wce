package com.powergem.wce.entities;

import java.util.Optional;

public record FlowgateEntity(int scenarioId, int id, int busid, double dfax, double trlim, String mon, String con, double rating, double loadingbefore, Optional<Integer> equipment_index) {

  public FlowgateEntity withEquipmentIndex(int index) {
    return new FlowgateEntity(scenarioId, id, busid, dfax, trlim, mon, con, rating, loadingbefore, Optional.of(index));
  }

}
