package com.dtcluster.model;

public record UnitSummary(long unitId, String unitName, int machines, int running, int idle, int down,
                          double utilizationPct, double kwh) {}
