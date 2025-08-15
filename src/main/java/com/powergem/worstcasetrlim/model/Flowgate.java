package com.powergem.worstcasetrlim.model;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.List;

@RecordBuilder
public record Flowgate(int id, int busid, double dfax, double trlim, String mon, String con, double rating, double loadingbefore, double loadingafter, double mwimpact, List<Harmer> harmers, int[] frBuses, int[] toBuses, int[] monType, int[] equipmentIndex) implements FlowgateBuilder.With {
}
