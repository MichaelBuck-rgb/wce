package com.powergem.wce.mapstruct;

import com.powergem.worstcasetrlim.model.Flowgate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface FlowgateMapper {
  FlowgateMapper INSTANCE = new FlowgateMapperImpl();

  @Mapping(target = "loadingafter", source = "flowgateEntity.loadingbefore")
  @Mapping(target = ".", source = "flowgateEntity")
  @Mapping(target = "harmers", source = "harmerEntities")
  Flowgate toFlowgate(Tuple tuple);
}
