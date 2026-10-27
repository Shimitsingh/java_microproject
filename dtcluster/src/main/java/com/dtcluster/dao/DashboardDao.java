package com.dtcluster.dao;

import com.dtcluster.db.Database;
import com.dtcluster.db.MongoDocs;
import com.dtcluster.model.MachineCard;
import com.dtcluster.model.MachineStatus;
import com.dtcluster.model.UnitSummary;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Dashboard projections calculated from MongoDB unit, machine, history, and energy collections. */
public final class DashboardDao {
    private DashboardDao() {}

    private static double utilization(long runningSeconds, double totalSeconds) {
        if (totalSeconds <= 0) return 0;
        return Math.min(100.0, Math.round((double) runningSeconds / totalSeconds * 1000.0) / 10.0);
    }

    public static List<UnitSummary> clusterSummary(int hours) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusHours(hours);
        Map<Long, List<Document>> linesByUnit = new HashMap<>();
        Database.collection("lines").find().forEach(line -> linesByUnit.computeIfAbsent(MongoDocs.number(line, "unitId"), ignored -> new ArrayList<>()).add(line));
        Map<Long, List<Document>> machinesByLine = new HashMap<>();
        Database.collection("machines").find().forEach(machine -> machinesByLine.computeIfAbsent(MongoDocs.number(machine, "lineId"), ignored -> new ArrayList<>()).add(machine));
        List<UnitSummary> summaries = new ArrayList<>();
        Database.collection("units").find().sort(new Document("_id", 1)).forEach(unit -> {
            long unitId = MongoDocs.id(unit);
            List<Document> unitMachines = new ArrayList<>();
            for (Document line : linesByUnit.getOrDefault(unitId, List.of())) unitMachines.addAll(machinesByLine.getOrDefault(MongoDocs.id(line), List.of()));
            int running = 0, idle = 0, down = 0;
            long totalRunningSeconds = 0;
            double energy = 0;
            for (Document machine : unitMachines) {
                long machineId = MongoDocs.id(machine);
                String status = machine.getString("status");
                if ("RUNNING".equals(status)) running++;
                else if ("IDLE".equals(status)) idle++;
                else if ("DOWN".equals(status)) down++;
                totalRunningSeconds += runningSeconds(machineId, start, now);
                energy += energy(machineId, start);
            }
            double util = utilization(totalRunningSeconds, (double) unitMachines.size() * hours * 3600);
            summaries.add(new UnitSummary(unitId, unit.getString("name"), unitMachines.size(), running, idle, down,
                    util, Math.round(energy * 100.0) / 100.0));
        });
        return summaries;
    }

    public static List<MachineCard> unitMachines(long unitId, int hours) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusHours(hours);
        List<MachineCard> cards = new ArrayList<>();
        for (var line : LineDao.findByUnit(unitId)) {
            for (var machine : MachineDao.findByLine(line.id())) {
                long running = runningSeconds(machine.id(), start, now);
                double kwh = energy(machine.id(), start);
                cards.add(new MachineCard(machine.id(), machine.name(), line.id(), line.name(), machine.status(),
                        utilization(running, hours * 3600.0), Math.round(kwh * 100.0) / 100.0));
            }
        }
        return cards;
    }

    private static long runningSeconds(long machineId, LocalDateTime windowStart, LocalDateTime now) {
        long seconds = 0;
        for (Document log : Database.collection("status_logs").find(Filters.and(
                Filters.eq("machineId", machineId), Filters.eq("status", "RUNNING")))) {
            LocalDateTime started = parse(log.getString("startedAt"));
            String endedValue = log.getString("endedAt");
            LocalDateTime ended = endedValue == null ? now : parse(endedValue);
            LocalDateTime from = started.isAfter(windowStart) ? started : windowStart;
            LocalDateTime to = ended.isBefore(now) ? ended : now;
            if (to.isAfter(from)) seconds += Duration.between(from, to).getSeconds();
        }
        return seconds;
    }

    private static double energy(long machineId, LocalDateTime windowStart) {
        double sum = 0;
        for (Document reading : Database.collection("energy_readings").find(Filters.eq("machineId", machineId))) {
            String at = reading.getString("recordedAt");
            if (at != null && !parse(at).isBefore(windowStart)) sum += ((Number) reading.get("kwh")).doubleValue();
        }
        return sum;
    }

    private static LocalDateTime parse(String value) {
        return LocalDateTime.parse(value);
    }
}
