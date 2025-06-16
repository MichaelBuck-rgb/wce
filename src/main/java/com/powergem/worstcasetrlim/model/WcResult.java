package com.powergem.worstcasetrlim.model;

import java.util.List;

public record WcResult(String version, String id, String title, List<Bus> buses, List<StressGen> StressGens, List<Flowgate> flowgates, List<BranchTerminal> branchTerminalList, String type, List<LineCostData> lineCostData, List<TransformerCostData> transformerCostData) {
}
