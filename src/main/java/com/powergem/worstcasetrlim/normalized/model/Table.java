package com.powergem.worstcasetrlim.normalized.model;

import java.util.List;

public record Table(List<String> schema, List<List<Object>> table) {
}
