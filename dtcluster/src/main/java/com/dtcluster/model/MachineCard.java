package com.dtcluster.model;

public record MachineCard(long id, String name, long lineId, String lineName, MachineStatus status,
                          double utilizationPct, double kwh) {}
