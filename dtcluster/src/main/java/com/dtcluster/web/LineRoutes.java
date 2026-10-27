package com.dtcluster.web;

import com.dtcluster.dao.LineDao;
import com.dtcluster.dao.MachineDao;
import com.dtcluster.model.ProductionLine;
import com.dtcluster.util.Http;
import com.dtcluster.util.Validate;
import io.javalin.Javalin;
import io.javalin.http.NotFoundResponse;

public final class LineRoutes {
    private LineRoutes() {}

    public static void register(Javalin app) {
        app.post("/api/lines", ctx -> {
            ProductionLine l = ctx.bodyAsClass(ProductionLine.class);
            Validate.notBlank(l.name(), "name");
            Validate.positive(l.unitId(), "unitId");
            Validate.positive(l.taktTimeSec(), "taktTimeSec");
            long id = LineDao.create(l);
            ctx.status(201).json(LineDao.findById(id).orElseThrow());
        });

        app.get("/api/lines/{id}", ctx -> ctx.json(
                LineDao.findById(Http.id(ctx)).orElseThrow(() -> new NotFoundResponse("Line not found"))));

        app.get("/api/lines/{id}/machines", ctx -> ctx.json(MachineDao.findByLine(Http.id(ctx))));

        app.delete("/api/lines/{id}", ctx -> {
            if (!LineDao.delete(Http.id(ctx))) throw new NotFoundResponse("Line not found");
            ctx.status(204);
        });
    }
}
