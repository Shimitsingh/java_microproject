package com.dtcluster.web;

import com.dtcluster.dao.DashboardDao;
import com.dtcluster.dao.UnitDao;
import com.dtcluster.util.Http;
import io.javalin.Javalin;
import io.javalin.http.NotFoundResponse;

public final class DashboardRoutes {
    private DashboardRoutes() {}

    public static void register(Javalin app) {
        // Cluster-level KPIs, one row per unit.   GET /api/dashboard/cluster?hours=24
        app.get("/api/dashboard/cluster", ctx -> ctx.json(DashboardDao.clusterSummary(Http.hours(ctx))));

        // Digital-twin view of one unit: every machine with live status + utilization + energy.
        app.get("/api/dashboard/units/{id}", ctx -> {
            long id = Http.id(ctx);
            if (UnitDao.findById(id).isEmpty()) throw new NotFoundResponse("Unit not found");
            ctx.json(DashboardDao.unitMachines(id, Http.hours(ctx)));
        });
    }
}
