package com.dtcluster.web;

import com.dtcluster.dao.MachineDao;
import com.dtcluster.model.Machine;
import com.dtcluster.util.Http;
import com.dtcluster.util.Validate;
import io.javalin.Javalin;
import io.javalin.http.NotFoundResponse;

public final class MachineRoutes {
    private MachineRoutes() {}

    public static void register(Javalin app) {
        app.get("/api/machines", ctx -> ctx.json(MachineDao.findAll()));

        app.get("/api/machines/{id}", ctx -> ctx.json(
                MachineDao.findById(Http.id(ctx)).orElseThrow(() -> new NotFoundResponse("Machine not found"))));

        app.post("/api/machines", ctx -> {
            Machine m = ctx.bodyAsClass(Machine.class);
            Validate.notBlank(m.name(), "name");
            Validate.positive(m.lineId(), "lineId");
            Validate.nonNegative(m.ratedPowerKw(), "ratedPowerKw");
            ctx.status(201).json(MachineDao.create(m));
        });

        app.delete("/api/machines/{id}", ctx -> {
            if (!MachineDao.delete(Http.id(ctx))) throw new NotFoundResponse("Machine not found");
            ctx.status(204);
        });
    }
}
