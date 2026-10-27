package com.dtcluster.dao;

import com.dtcluster.db.Database;
import com.dtcluster.db.MongoDocs;
import com.dtcluster.model.ProductionLine;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class LineDao {
    private LineDao() {}
    private static ProductionLine fromDoc(Document d) {
        return new ProductionLine(MongoDocs.id(d), MongoDocs.number(d, "unitId"), d.getString("name"), d.getInteger("taktTimeSec"));
    }
    public static List<ProductionLine> findByUnit(long unitId) {
        List<ProductionLine> out = new ArrayList<>();
        Database.collection("lines").find(Filters.eq("unitId", unitId)).sort(new Document("_id", 1)).forEach(d -> out.add(fromDoc(d)));
        return out;
    }
    public static Optional<ProductionLine> findById(long id) {
        Document d = Database.collection("lines").find(Filters.eq("_id", id)).first();
        return Optional.ofNullable(d).map(LineDao::fromDoc);
    }
    public static long create(ProductionLine line) {
        long id = Database.nextId("lines");
        Database.collection("lines").insertOne(new Document("_id", id).append("unitId", line.unitId()).append("name", line.name()).append("taktTimeSec", line.taktTimeSec()));
        return id;
    }
    public static boolean delete(long id) {
        if (findById(id).isEmpty()) return false;
        MachineDao.findByLine(id).forEach(m -> MachineDao.delete(m.id()));
        return Database.collection("lines").deleteOne(Filters.eq("_id", id)).getDeletedCount() > 0;
    }
}
