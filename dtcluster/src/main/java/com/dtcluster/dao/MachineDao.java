package com.dtcluster.dao;

import com.dtcluster.db.Database;
import com.dtcluster.db.MongoDocs;
import com.dtcluster.model.Machine;
import com.dtcluster.model.MachineStatus;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MachineDao {
    private MachineDao() {}
    private static Machine fromDoc(Document d) {
        return new Machine(MongoDocs.id(d), MongoDocs.number(d, "lineId"), d.getString("name"), d.getString("machineType"), d.getDouble("ratedPowerKw"), MachineStatus.valueOf(d.getString("status")));
    }
    public static List<Machine> findAll() {
        List<Machine> out = new ArrayList<>();
        Database.collection("machines").find().sort(new Document("_id", 1)).forEach(d -> out.add(fromDoc(d)));
        return out;
    }
    public static List<Machine> findByLine(long lineId) {
        List<Machine> out = new ArrayList<>();
        Database.collection("machines").find(Filters.eq("lineId", lineId)).sort(new Document("_id", 1)).forEach(d -> out.add(fromDoc(d)));
        return out;
    }
    public static List<Machine> findByUnit(long unitId) {
        List<Machine> out = new ArrayList<>();
        for (var line : LineDao.findByUnit(unitId)) out.addAll(findByLine(line.id()));
        return out;
    }
    public static Optional<Machine> findById(long id) {
        Document d = Database.collection("machines").find(Filters.eq("_id", id)).first();
        return Optional.ofNullable(d).map(MachineDao::fromDoc);
    }
    public static Machine create(Machine m) {
        long id = Database.nextId("machines");
        LocalDateTime now = LocalDateTime.now();
        Database.collection("machines").insertOne(new Document("_id", id).append("lineId", m.lineId()).append("name", m.name())
                .append("machineType", m.machineType()).append("ratedPowerKw", m.ratedPowerKw()).append("status", "IDLE"));
        Database.collection("status_logs").insertOne(new Document("machineId", id).append("status", "IDLE").append("startedAt", now.toString()).append("endedAt", null));
        return new Machine(id, m.lineId(), m.name(), m.machineType(), m.ratedPowerKw(), MachineStatus.IDLE);
    }
    public static boolean delete(long id) {
        Database.collection("status_logs").deleteMany(Filters.eq("machineId", id));
        Database.collection("energy_readings").deleteMany(Filters.eq("machineId", id));
        return Database.collection("machines").deleteOne(Filters.eq("_id", id)).getDeletedCount() > 0;
    }
}
