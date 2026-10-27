# ForgeOS Digital Twin Dashboard

A presentation-ready manufacturing digital twin built with Java 17, Javalin, MongoDB, and a responsive browser dashboard.

## Quick start (Windows)

Requirements: Java 17+ and MongoDB Community running at `mongodb://127.0.0.1:27017`. The included wrapper downloads Maven on first use.

1. Open a terminal in the `dtcluster` folder.
2. Start MongoDB if it is not already running (MongoDB Compass alone is only a UI; the MongoDB server must also be running).
3. Start the application:

   ```powershell
   .\mvnw.cmd compile exec:java
   ```

4. Open <http://localhost:7070>.
5. In a second terminal in the same folder, start live simulated machine readings:

   ```powershell
   .\mvnw.cmd exec:java "-Dexec.mainClass=com.dtcluster.sim.TelemetrySimulator"
   ```

The first application launch automatically creates the `dtcluster` database and loads an illustrative Peenya Industrial Cluster if the `units` collection is empty. The sample includes two units, three lines, five machines, shared resources, status history, and demo energy readings. The seed runs only once and never overwrites existing unit data.

## MongoDB settings

Defaults are in `src/main/resources/application.properties`:

- URI: `mongodb://127.0.0.1:27017`
- Database: `dtcluster`
- HTTP port: `7070`

The included `mvnw.cmd` downloads Maven automatically on first use, so you do not need a global Maven installation. Override with environment variables `MONGODB_URI`, `MONGODB_DATABASE`, and `PORT`. Example for PowerShell:

```powershell
$env:MONGODB_URI = 'mongodb://127.0.0.1:27017'
$env:MONGODB_DATABASE = 'dtcluster'
$env:PORT = '7070'
.\mvnw.cmd compile exec:java
```

## What the dashboard shows

- Cluster KPIs: units, running machines, utilization, and energy.
- Unit cards with machine mix and utilization; select a unit to drill into its fleet.
- Per-machine status, line assignment, utilization, and energy use.
- Operations highlights for fleet health, idle/down machines, utilization leader, and energy.
- A time-window selector and live refresh every five seconds.
- Three shared cluster resources: testing lab, boiler, and forklift.

## API

| Method | Path | Purpose |
| --- | --- | --- |
| GET/POST | `/api/units` | list / register a unit |
| GET/PUT/DELETE | `/api/units/{id}` | read / update / delete a unit |
| GET | `/api/units/{id}/lines`, `/api/units/{id}/machines` | unit children |
| POST | `/api/lines` | register a production line |
| GET/POST | `/api/machines` | list / register a machine |
| POST | `/api/telemetry` | receive machine state and energy |
| GET | `/api/dashboard/cluster?hours=24` | unit-level KPIs |
| GET | `/api/dashboard/units/{id}?hours=24` | machine digital-twin view |

## Data model

MongoDB collections: `units`, `lines`, `machines`, `status_logs`, `energy_readings`, `shared_resources`, and `sequences`. Entity references use numeric IDs to preserve the API contract. Telemetry history is kept separately so status changes and energy measurements can be inspected independently.

## Presentation walkthrough

See [`DEMO_GUIDE.md`](../DEMO_GUIDE.md) for the short walkthrough and troubleshooting tips.



