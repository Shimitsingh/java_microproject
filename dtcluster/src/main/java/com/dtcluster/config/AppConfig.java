package com.dtcluster.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Loads application.properties, allowing environment overrides for MongoDB and the HTTP port. */
public final class AppConfig {
    private AppConfig() {}

    public static Properties load() {
        Properties p = new Properties();
        try (InputStream in = AppConfig.class.getResourceAsStream("/application.properties")) {
            if (in == null) throw new IllegalStateException("application.properties not found");
            p.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read application.properties", e);
        }
        override(p, "mongo.uri", "MONGODB_URI");
        override(p, "mongo.database", "MONGODB_DATABASE");
        override(p, "server.port", "PORT");
        return p;
    }

    private static void override(Properties p, String key, String env) {
        String value = System.getenv(env);
        if (value != null && !value.isBlank()) p.setProperty(key, value);
    }
}
