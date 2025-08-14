package com.powergem.wce.mapstruct;

import com.powergem.wce.entities.FlowgateEntity;
import com.powergem.wce.entities.HarmerEntity;

import java.util.List;

public record Tuple(FlowgateEntity flowgateEntity, List<HarmerEntity> harmerEntities, int[] frBuses, int[] toBuses,
                    int[] monType, int[] equipmentIndex) {
}
