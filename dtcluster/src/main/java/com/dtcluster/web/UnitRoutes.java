package com.dtcluster.web;

import com.dtcluster.dao.LineDao;
import com.dtcluster.dao.MachineDao;
import com.dtcluster.dao.UnitDao;
import com.dtcluster.model.Unit;
import com.dtcluster.util.Http;
import com.dtcluster.util.Validate;
import io.javalin.Javalin;
import io.javalin.http.NotFoundResponse;

public final class UnitRoutes {
    private UnitRoutes() {}

    public static void register(Javalin app) {
        app.get("/api/units", ctx -> ctx.json(UnitDao.findAll()));

        app.get("/api/units/{id}", ctx -> ctx.json(
                UnitDao.findById(Http.id(ctx)).orElseThrow(() -> new NotFoundResponse("Unit not found"))));

        app.post("/api/units", ctx -> {
            Unit u = ctx.bodyAsClass(Unit.class);
            Validate.notBlank(u.name(), "name");
            Validate.positive(u.clusterId(), "clusterId");
            long id = UnitDao.create(u);
            ctx.status(201).json(UnitDao.findById(id).orElseThrow());
        });

        app.put("/api/units/{id}", ctx -> {
            long id = Http.id(ctx);
            Unit u = ctx.bodyAsClass(Unit.class);
            Validate.notBlank(u.name(), "name");
            if (!UnitDao.update(id, u)) throw new NotFoundResponse("Unit not found");
            ctx.json(UnitDao.findById(id).orElseThrow());
        });

        app.delete("/api/units/{id}", ctx -> {
            if (!UnitDao.delete(Http.id(ctx))) throw new NotFoundResponse("Unit not found");
            ctx.status(204);
        });

        app.get("/api/units/{id}/lines", ctx -> ctx.json(LineDao.findByUnit(Http.id(ctx))));
        app.get("/api/units/{id}/machines", ctx -> ctx.json(MachineDao.findByUnit(Http.id(ctx))));
    }
}
