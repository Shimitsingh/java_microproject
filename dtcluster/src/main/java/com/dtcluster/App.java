package com.dtcluster;

import com.dtcluster.config.AppConfig;
import com.dtcluster.db.Database;
import com.dtcluster.db.DemoData;
import com.dtcluster.service.DbTelemetryService;
import com.dtcluster.web.*;
import com.mongodb.MongoException;
import com.mongodb.client.model.IndexOptions;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.util.Map;
import java.util.Properties;

public class App {
    public static void main(String[] args) {
        Properties props = AppConfig.load();
        Database.init(props);
        DemoData.seedIfEmpty();

        Javalin app = Javalin.create(cfg -> cfg.staticFiles.add("/public", Location.CLASSPATH));
        UnitRoutes.register(app);
        LineRoutes.register(app);
        MachineRoutes.register(app);
        TelemetryRoutes.register(app, new DbTelemetryService());
        DashboardRoutes.register(app);
        app.exception(MongoException.class, (e, ctx) -> {
            e.printStackTrace();
            ctx.status(500).json(Map.of("error", "MongoDB operation failed", "detail", e.getMessage() == null ? "Unknown database error" : e.getMessage()));
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { app.stop(); Database.close(); }));
        app.start(Integer.parseInt(props.getProperty("server.port", "7070")));
    }
}
