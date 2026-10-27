package com.dtcluster.dao;

import com.dtcluster.db.Database;
import com.dtcluster.db.MongoDocs;
import com.dtcluster.model.Unit;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class UnitDao {
    private UnitDao() {}
    private static Document toDoc(Unit u, long id) {
        return new Document("_id", id).append("clusterId", u.clusterId()).append("name", u.name())
                .append("location", u.location()).append("ownerName", u.ownerName());
    }
    private static Unit fromDoc(Document d) {
        return new Unit(MongoDocs.id(d), MongoDocs.number(d, "clusterId"), d.getString("name"), d.getString("location"), d.getString("ownerName"));
    }
    public static List<Unit> findAll() {
        List<Unit> out = new ArrayList<>();
        Database.collection("units").find().sort(new Document("_id", 1)).forEach(d -> out.add(fromDoc(d)));
        return out;
    }
    public static Optional<Unit> findById(long id) {
        Document d = Database.collection("units").find(Filters.eq("_id", id)).first();
        return Optional.ofNullable(d).map(UnitDao::fromDoc);
    }
    public static long create(Unit u) {
        long id = Database.nextId("units");
        Database.collection("units").insertOne(toDoc(u, id));
        return id;
    }
    public static boolean update(long id, Unit u) {
        return Database.collection("units").replaceOne(Filters.eq("_id", id), toDoc(u, id)).getModifiedCount() > 0;
    }
    public static boolean delete(long id) {
        if (findById(id).isEmpty()) return false;
        List<Long> lineIds = new ArrayList<>();
        Database.collection("lines").find(Filters.eq("unitId", id)).forEach(d -> lineIds.add(MongoDocs.id(d)));
        List<Long> machineIds = new ArrayList<>();
        if (!lineIds.isEmpty()) Database.collection("machines").find(Filters.in("lineId", lineIds)).forEach(d -> machineIds.add(MongoDocs.id(d)));
        if (!machineIds.isEmpty()) {
            Database.collection("status_logs").deleteMany(Filters.in("machineId", machineIds));
            Database.collection("energy_readings").deleteMany(Filters.in("machineId", machineIds));
            Database.collection("machines").deleteMany(Filters.in("_id", machineIds));
        }
        if (!lineIds.isEmpty()) Database.collection("lines").deleteMany(Filters.in("_id", lineIds));
        return Database.collection("units").deleteOne(Filters.eq("_id", id)).getDeletedCount() > 0;
    }
}
