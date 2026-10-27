package com.dtcluster.model;

import java.time.LocalDateTime;

/** One reading from a machine (or from an IoT gateway in future). timestamp and kWh are optional. */
public record TelemetryReading(long machineId, MachineStatus status, Double kWh, LocalDateTime timestamp) {}
