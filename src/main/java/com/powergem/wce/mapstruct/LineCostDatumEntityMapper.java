package com.powergem.wce.mapstruct;

import com.powergem.wce.entities.LineCostDatumEntity;
import com.powergem.worstcasetrlim.model.LineCostData;
import org.mapstruct.Mapper;

@Mapper
public interface LineCostDatumEntityMapper {
  LineCostDatumEntityMapper INSTANCE = new LineCostDatumEntityMapperImpl();

  LineCostData toFlowgate(LineCostDatumEntity entity);
}
