package com.dtcluster.service;

import com.dtcluster.model.TelemetryReading;

/**
 * The seam for future IoT integration. Today readings arrive over HTTP; later an MQTT
 * subscriber or OPC-UA gateway can call ingest() too, and nothing else in the app changes.
 */
public interface TelemetryService {
    /** @return false if the machine id is unknown */
    boolean ingest(TelemetryReading reading);
}
