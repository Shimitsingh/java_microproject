package com.dtcluster.model;

public record Machine(long id, long lineId, String name, String machineType,
                      double ratedPowerKw, MachineStatus status) {}
