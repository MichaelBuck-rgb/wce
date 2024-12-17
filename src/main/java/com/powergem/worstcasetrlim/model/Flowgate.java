package com.powergem.worstcasetrlim.model;

import java.util.List;

public record Flowgate(int id, int busid, double dfax, double trlim, String mon, String con, double rating, double loadingbefore, double loadingafter, double mwimpact, List<Harmer> harmers, int[] frBuses, int[] toBuses, int[] monType) {
}
