package com.powergem.wce.mapstruct;

import com.powergem.wce.entities.StressGenEntity;
import com.powergem.worstcasetrlim.model.StressGen;
import org.mapstruct.Mapper;

@Mapper
public interface StressGenMapper {
  StressGenMapper INSTANCE = new StressGenMapperImpl();

  StressGen toStressGen(StressGenEntity entity);
}
