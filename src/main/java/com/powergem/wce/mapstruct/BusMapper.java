package com.powergem.wce.mapstruct;

import com.powergem.wce.entities.BusEntity;
import com.powergem.worstcasetrlim.model.Bus;
import org.mapstruct.Mapper;

@Mapper
public interface BusMapper {
  BusMapper INSTANCE = new BusMapperImpl();

  Bus toBus(BusEntity entity);
}
