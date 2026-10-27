# ForgeOS Digital Twin — Demo Guide

## Story to tell

ForgeOS is a live digital twin for a manufacturing cluster. It gives an operator a shared view of production health, machine utilization, and energy use across independently managed factories.

The first-run demo data models the Peenya Industrial Cluster in Bengaluru: two manufacturers, three production lines, five machines, and shared testing, utility, and logistics resources. Sample telemetry is labeled `demo-seed`; the simulator then adds live readings every three seconds.

## Start the demo

Requirements: Java 17+ and a running MongoDB Community server; the included wrapper downloads Maven on first use at `mongodb://127.0.0.1:27017`.

1. Open PowerShell in the `dtcluster` project folder.
2. Start the app:

   ```powershell
   .\mvnw.cmd compile exec:java
   ```

   On first run, MongoDB creates the `dtcluster` database and the app seeds the demo records if there are no units yet.

3. Open <http://localhost:7070>.
4. In a second PowerShell window, from the same folder, start the telemetry simulator:

   ```powershell
   .\mvnw.cmd exec:java "-Dexec.mainClass=com.dtcluster.sim.TelemetrySimulator"
   ```

Keep both terminals open while presenting. Stop the simulator and server with Ctrl+C. If MongoDB is installed but the app cannot connect, confirm the MongoDB **server/service** is running; Compass by itself does not start the database server.

## Suggested 90-second walkthrough

1. Start with the four KPI cards: connected facilities, machine state, utilization, and energy.
2. Explain that the utilization chart compares units for the selected time window.
3. Select a unit, then show its lines and machine status cards.
4. Start the simulator to show status, energy, and operations highlights changing live.
5. Finish on the automatic operations summary: fleet health, machines needing attention, top utilization, and energy use.

## Project highlights

- Responsive dark operations interface with no external front-end packages or CDN dependency.
- Java 17 and Javalin API, official MongoDB synchronous Java driver, and local MongoDB persistence.
- Separate MongoDB collections for current assets, status history, and energy readings.
- Sample data is inserted only when the database has no units; existing data is preserved.

## Fast troubleshooting

- **MongoDB connection refused:** start the MongoDB Server service and retry. Default URI is `mongodb://127.0.0.1:27017`.
- **Port 7070 busy:** set a different PowerShell value, for example `$env:PORT='7071'`, then restart.
- **Dashboard says API unavailable:** keep the Java app running and reload <http://localhost:7070>.
- **No live movement:** leave the simulator terminal running; it sends status and energy every three seconds.
- **Already used this database:** the seed intentionally does not reset or overwrite existing records.


