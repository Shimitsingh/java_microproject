package com.dtcluster.db;

import com.dtcluster.model.MachineStatus;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.time.LocalDateTime;
import java.util.List;

/** Seeds an illustrative Peenya cluster once, only when the units collection is empty. */
public final class DemoData {
    private DemoData() {}

    public static void seedIfEmpty() {
        MongoCollection<Document> units = Database.collection("units");
        if (units.countDocuments() > 0) return;
        LocalDateTime now = LocalDateTime.now();
        units.insertMany(List.of(
                new Document("_id", 1L).append("clusterId", 1L).append("name", "Sri Ganesh Precision Works").append("location", "Peenya 2nd Stage, Bengaluru").append("ownerName", "R. Ganesh"),
                new Document("_id", 2L).append("clusterId", 1L).append("name", "Karnataka Auto Components").append("location", "Peenya 1st Stage, Bengaluru").append("ownerName", "S. Rao")));
        Database.collection("lines").insertMany(List.of(
                new Document("_id", 1L).append("unitId", 1L).append("name", "CNC Line A").append("taktTimeSec", 45),
                new Document("_id", 2L).append("unitId", 1L).append("name", "Assembly Line").append("taktTimeSec", 60),
                new Document("_id", 3L).append("unitId", 2L).append("name", "Press Line 1").append("taktTimeSec", 30)));
        List<Document> machines = List.of(
                machine(1, 1, "CNC-01", "CNC Lathe", 15.0, MachineStatus.RUNNING),
                machine(2, 1, "CNC-02", "CNC Mill", 18.5, MachineStatus.IDLE),
                machine(3, 2, "ASM-01", "Assembly Station", 4.0, MachineStatus.RUNNING),
                machine(4, 3, "PRS-01", "Hydraulic Press", 22.0, MachineStatus.DOWN),
                machine(5, 3, "PRS-02", "Hydraulic Press", 22.0, MachineStatus.RUNNING));
        Database.collection("machines").insertMany(machines);
        for (Document machine : machines) {
            long id = machine.getLong("_id");
            String status = machine.getString("status");
            Database.collection("status_logs").insertOne(new Document("machineId", id)
                    .append("status", status).append("startedAt", now.minusHours(6).toString()).append("endedAt", null));
            // Clearly labeled illustrative sample readings provide a useful first-run dashboard.
            for (int i = 0; i < 8; i++) {
                double load = status.equals("RUNNING") ? 0.72 : status.equals("IDLE") ? 0.1 : 0.0;
                Database.collection("energy_readings").insertOne(new Document("machineId", id)
                        .append("recordedAt", now.minusHours(7 - i).toString())
                        .append("kwh", machine.getDouble("ratedPowerKw") * load)
                        .append("source", "demo-seed"));
            }
        }
        Database.collection("shared_resources").insertMany(List.of(
                new Document("clusterId", 1L).append("name", "Common Testing Lab").append("type", "LAB").append("availability", "SHARED"),
                new Document("clusterId", 1L).append("name", "Shared Boiler").append("type", "UTILITY").append("availability", "SHARED"),
                new Document("clusterId", 1L).append("name", "Cluster Forklift").append("type", "LOGISTICS").append("availability", "SHARED")));
        Database.ensureSequenceAtLeast("units", 2);
        Database.ensureSequenceAtLeast("lines", 3);
        Database.ensureSequenceAtLeast("machines", 5);
        System.out.println("Loaded demo cluster: 2 units, 3 lines, 5 machines, and shared resources.");
    }

    private static Document machine(long id, long lineId, String name, String type, double power, MachineStatus status) {
        return new Document("_id", id).append("lineId", lineId).append("name", name)
                .append("machineType", type).append("ratedPowerKw", power).append("status", status.name());
    }
}
