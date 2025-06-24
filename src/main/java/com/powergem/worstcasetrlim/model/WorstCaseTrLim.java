package com.powergem.worstcasetrlim.model;

import io.avaje.jsonb.Json;

import java.util.List;

@Json
public record WorstCaseTrLim(List<WcResult> wcResults) {
}
