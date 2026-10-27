package com.dtcluster.dao;

import com.dtcluster.db.Database;
import com.dtcluster.model.TelemetryReading;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import java.time.LocalDateTime;

/** Stores machine state history and energy readings in separate MongoDB collections. */
public final class TelemetryDao {
    private TelemetryDao() {}

    public static boolean record(TelemetryReading reading) {
        LocalDateTime timestamp = reading.timestamp() != null ? reading.timestamp() : LocalDateTime.now();
        Document machine = Database.collection("machines").find(Filters.eq("_id", reading.machineId())).first();
        if (machine == null) return false;
        String current = machine.getString("status");
        String next = reading.status().name();
        if (!next.equals(current)) {
            long changed = Database.collection("machines").updateOne(
                    Filters.and(Filters.eq("_id", reading.machineId()), Filters.eq("status", current)),
                    Updates.set("status", next)).getModifiedCount();
            if (changed > 0) {
                Database.collection("status_logs").updateOne(
                        Filters.and(Filters.eq("machineId", reading.machineId()), Filters.eq("endedAt", null)),
                        Updates.set("endedAt", timestamp.toString()));
                Database.collection("status_logs").insertOne(new Document("machineId", reading.machineId())
                        .append("status", next).append("startedAt", timestamp.toString()).append("endedAt", null));
            }
        }
        if (reading.kWh() != null) {
            Database.collection("energy_readings").insertOne(new Document("machineId", reading.machineId())
                    .append("recordedAt", timestamp.toString()).append("kwh", reading.kWh()).append("source", "telemetry"));
        }
        return true;
    }
}
