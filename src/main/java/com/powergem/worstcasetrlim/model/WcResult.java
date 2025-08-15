package com.powergem.worstcasetrlim.model;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.List;

@RecordBuilder
public record WcResult(String version, String id, String title, List<Bus> buses, List<StressGen> StressGens, List<Flowgate> flowgates, List<BranchTerminal> branchTerminalList, String type, List<LineCostData> lineCostData, List<TransformerCostData> transformerCostData) implements WcResultBuilder.With {
}
