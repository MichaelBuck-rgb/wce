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
  @Mapping(target = "mwimpact", constant = "0")
  @Mapping(target = "with", ignore = true)
  @Mapping(target = "withId", ignore = true)
  @Mapping(target = "withBusid", ignore = true)
  @Mapping(target = "withDfax", ignore = true)
  @Mapping(target = "withTrlim", ignore = true)
  @Mapping(target = "withMon", ignore = true)
  @Mapping(target = "withCon", ignore = true)
  @Mapping(target = "withRating", ignore = true)
  @Mapping(target = "withLoadingbefore", ignore = true)
  @Mapping(target = "withLoadingafter", ignore = true)
  @Mapping(target = "withMwimpact", ignore = true)
  @Mapping(target = "withHarmers", ignore = true)
  @Mapping(target = "withFrBuses", ignore = true)
  @Mapping(target = "withToBuses", ignore = true)
  @Mapping(target = "withMonType", ignore = true)
  @Mapping(target = "withEquipmentIndex", ignore = true)
  Flowgate toFlowgate(Tuple tuple);
}
