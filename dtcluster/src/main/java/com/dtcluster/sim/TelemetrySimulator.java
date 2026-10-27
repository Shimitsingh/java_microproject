package com.dtcluster.sim;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Pretends to be the shop floor: every few seconds each machine reports a status and the
 * energy it used. Run it while the server is up and watch the dashboard move.
 *   mvn exec:java -Dexec.mainClass=com.dtcluster.sim.TelemetrySimulator
 */
public class TelemetrySimulator {
    private static final String BASE = System.getenv().getOrDefault("API_BASE", "http://localhost:7070");
    private static final int INTERVAL_SEC = 3;

    public static void main(String[] args) throws Exception {
        ObjectMapper json = new ObjectMapper();
        HttpClient http = HttpClient.newHttpClient();
        Random rnd = new Random();

        HttpResponse<String> res = http.send(
                HttpRequest.newBuilder(URI.create(BASE + "/api/machines")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        List<Map<String, Object>> machines = json.readValue(res.body(), new TypeReference<>() {});
        if (machines.isEmpty()) { System.out.println("No machines registered yet."); return; }

        Map<Long, String> state = new HashMap<>();
        for (Map<String, Object> m : machines) state.put(((Number) m.get("id")).longValue(), "IDLE");
        System.out.println("Simulating " + machines.size() + " machines. Ctrl+C to stop.");

        while (true) {
            for (Map<String, Object> m : machines) {
                long id = ((Number) m.get("id")).longValue();
                double ratedKw = ((Number) m.get("ratedPowerKw")).doubleValue();
                String next = nextState(state.get(id), rnd);
                state.put(id, next);

                double load = switch (next) {
                    case "RUNNING" -> 0.75 + rnd.nextDouble() * 0.25;
                    case "IDLE" -> 0.10;
                    default -> 0.0;
                };
                double kwh = ratedKw * load * INTERVAL_SEC / 3600.0;

                Map<String, Object> body = Map.of(
                        "machineId", id, "status", next, "kWh", kwh,
                        "timestamp", LocalDateTime.now().withNano(0).toString());
                HttpResponse<String> r = http.send(
                        HttpRequest.newBuilder(URI.create(BASE + "/api/telemetry"))
                                .header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build(),
                        HttpResponse.BodyHandlers.ofString());
                if (r.statusCode() != 202) System.out.println("Machine " + id + " -> HTTP " + r.statusCode());
            }
            Thread.sleep(INTERVAL_SEC * 1000L);
        }
    }

    private static String nextState(String cur, Random rnd) {
        double p = rnd.nextDouble();
        return switch (cur) {
            case "RUNNING" -> p < 0.90 ? "RUNNING" : p < 0.97 ? "IDLE" : "DOWN";
            case "IDLE" -> p < 0.50 ? "RUNNING" : "IDLE";
            default -> p < 0.30 ? "IDLE" : "DOWN";
        };
    }
}
