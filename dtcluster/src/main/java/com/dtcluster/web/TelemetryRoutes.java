package com.dtcluster.web;

import com.dtcluster.model.TelemetryReading;
import com.dtcluster.service.TelemetryService;
import com.dtcluster.util.Validate;
import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.NotFoundResponse;

public final class TelemetryRoutes {
    private TelemetryRoutes() {}

    /** POST /api/telemetry  {"machineId":1,"status":"RUNNING","kWh":0.05,"timestamp":"2026-10-10T10:15:00"} */
    public static void register(Javalin app, TelemetryService service) {
        app.post("/api/telemetry", ctx -> {
            TelemetryReading r = ctx.bodyAsClass(TelemetryReading.class);
            Validate.positive(r.machineId(), "machineId");
            if (r.status() == null) throw new BadRequestResponse("status is required (RUNNING, IDLE, DOWN)");
            if (r.kWh() != null) Validate.nonNegative(r.kWh(), "kWh");
            if (!service.ingest(r)) throw new NotFoundResponse("Machine not found");
            ctx.status(202);
        });
    }
}
