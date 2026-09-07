# Car Export Algeria

A comparison platform for vehicles under 3 years old, intended for export to Algeria. The application collects listings from multiple dealer sites and garages, then highlights the best price per model.

Monorepo containing the backend (API) and the frontend (web interface).

## Project structure

```
car-export-algeria/
├── backend/     Spring Boot API
└── frontend/    Angular interface
```

## Tech stack

| | |
|---|---|
| **Backend** | Java 21, Spring Boot 3.3, Spring Data JPA, H2 (default) / PostgreSQL (persistent, via Docker Compose), Jsoup, Playwright |
| **Frontend** | Angular 18 (standalone components), TypeScript, RxJS |

## How it works

User searches only query data already collected in the database — no real-time scraping. A scheduled job periodically refreshes the data in the background, which keeps API responses fast and reliable regardless of the external sites' availability.

### Scraping: Jsoup vs Playwright

Two scraping techniques are available side by side, picked per source depending on how that site renders its content:

| | Jsoup | Playwright |
|---|---|---|
| **How it works** | Fetches raw HTML over HTTP, no JS execution | Drives a real headless browser |
| **Use when** | The site renders listing HTML server-side | The site loads listings via client-side JavaScript (SPA, infinite scroll, "load more" buttons) |
| **Cost** | Lightweight, fast | Heavier — launches an actual browser |
| **Example connector** | `GarageXConnector` | `DynamicMarketplaceConnector` |

Both implement the same `VehicleSourceConnector` interface, so `ScrapingOrchestrator` runs them identically — the technique is an implementation detail of each connector, not something the rest of the system needs to know about.

The Playwright browser (`PlaywrightBrowserManager`) starts lazily on first use rather than at application startup, so a missing browser installation only affects that specific connector — not the whole app. If you add a Playwright-based connector, install the browser binaries once:

```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
```

## Algerian import regulations

The backend enforces the import rules for private individuals (Decret executif n. 23-74 and the finance law) directly at the search level — non-eligible vehicles never appear in the results:

- **3-year age rule**: computed to the day between first registration and today, with a 2-year-10-month safety margin applied instead of the full 3 years, to absorb delays before customs clearance.
- **Allowed fuel types only**: Essence ⛽, Hybrid 🔋 and Electric ⚡. Diesel 🚫 is strictly banned for private import of vehicles under 3 years old.
- **Customs duty reduction**, shown for each result:
  - Electric: -80%
  - Essence/Hybrid ≤ 1800 cm³: -50%
  - Essence/Hybrid > 1800 cm³: -20%

This logic lives in `ImportEligibilityService`, kept separate from the search logic so it can evolve independently if the regulation changes.

## RoRo shipping cost estimator

Estimates Roll-on/Roll-off freight costs for the supported Europe → Algeria routes (Marseille / Alicante / Sete → Alger / Oran / Bejaia). Base freight rates are indicative placeholders in `ShippingCostService` — replace them with real carrier rates before relying on this for actual budgeting.

Rather than a single global widget, each vehicle card has its own "Estimate shipping cost" link. Clicking it fetches an estimate for a default route (`ShippingSelectionService`), and an "Edit" button lets the user pick a different origin/destination for that specific vehicle without affecting other cards. The port-selection dropdown itself (`PortSelectorComponent`) is shared between the inline estimate and the edit popup to avoid duplicating the same form twice.

`GET /api/shipping/estimate?originPort=&destinationPort=` — freight cost breakdown (base rate + handling fee)

## Scraping source health monitoring

Each scheduled scrape round records, per source, whether the last run succeeded and how many vehicles it produced. This makes a silently-broken marketplace visible immediately (e.g. a site that stays online but serves a "deployment paused" banner instead of the listing grid) instead of looking healthy by accident.

Per-source status is one of:

| Status | Meaning |
|---|---|
| `UP` | Last round succeeded and produced vehicles |
| `EMPTY` | Last round succeeded but produced zero vehicles — the site is probably down or changed its markup |
| `DOWN` | Last round threw (network error, HTTP failure, parse error) |

The overall status folds the per-source statuses together (`DOWN` wins over `EMPTY` wins over `UP`), and is `UNKNOWN` before the first round.

### Endpoints

| Endpoint | Description |
|---|---|
| `GET /api/health` | Health snapshot from the last scrape round (scheduled or manual) |
| `POST /api/health/refresh` | Runs a full refresh round **now** (same code path as the 6-hourly scheduler: fetch, persist, evict the search cache) and returns the fresh snapshot |

```bash
# Inspect the last round
curl http://localhost:8080/api/health

# Force a round now (can take a couple of minutes when a marketplace is back online)
curl -X POST http://localhost:8080/api/health/refresh
```

Example response:

```json
{
  "status": "EMPTY",
  "sources": [
    { "source": "CarXport",     "status": "UP",    "successCount": 12, "failureCount": 0, "consecutiveFailures": 0, "lastVehicleCount": 24 },
    { "source": "ExportCar213", "status": "EMPTY", "successCount": 11, "failureCount": 1, "consecutiveFailures": 0, "lastVehicleCount": 0 }
  ]
}
```

Notes:

- A source in failure mode never deletes its existing rows — `ListingUpdateService` only inserts/updates, so search results keep serving the last-known data.
- The per-source counters (`successCount`, `failureCount`, `consecutiveFailures`) are in-memory and reset on backend restart.
- Sniffing this endpoint from Kubernetes-style probes is fine for liveness; it does not hit the network.

## Security

The API is stateless and guarded by HTTP Basic authentication, configured in `SecurityConfig.java`. Authorization is enforced at **two levels**:
1. **URL-based rules** declared in the `SecurityFilterChain` (path → role rules below) — the primary gate in `SecurityConfig.java`;
2. **Method-level** `@PreAuthorize("hasRole('ADMIN')")` on `ScrapingHealthController.refresh()` as a defense-in-depth layer (backed by `@EnableMethodSecurity`), so the endpoint stays protected even if URL rules are ever loosened.

### Access rules

| Path | Access |
|---|---|
| `GET /api/**` | Public (`permitAll`) — read-only search & health data |
| `POST /api/health/refresh` | Admin only (`hasRole("ADMIN")`) — triggers an on-demand scrape round |
| Any other request | Denied (`denyAll`) |

- Only a single in-memory admin account exists, configured via `app.security.admin.username` / `app.security.admin.password`.
- The password is hashed with BCrypt (`BCryptPasswordEncoder`) — never stored or transmitted in plain text.
- CSRF protection is disabled: the API is stateless and the credentials are sent in the `Authorization` header (HTTP Basic), which browsers never attach automatically, so there is no session to forge. If you ever move to cookie-based or session auth, re-enable CSRF.
- CORS is enabled through the standard Spring configuration.

### Environment variables

Credentials are externalized, never hard-coded. In `application.properties` they are bound with defaults:

```properties
app.security.admin.username=${APP_SECURITY_ADMIN_USERNAME:admin}
app.security.admin.password=${APP_SECURITY_ADMIN_PASSWORD:change-me-now}
```

Spring Boot reads `APP_SECURITY_ADMIN_USERNAME` / `APP_SECURITY_ADMIN_PASSWORD` from the environment and falls back to the defaults (`admin` / `change-me-now`) when they are not set. Set them however your environment handles env vars:

**Windows (PowerShell):**
```powershell
$env:APP_SECURITY_ADMIN_USERNAME="admin"
$env:APP_SECURITY_ADMIN_PASSWORD="s3cret!Str0ng"
cd backend
./mvnw spring-boot:run
```

**Linux/macOS (bash):**
```bash
export APP_SECURITY_ADMIN_USERNAME=admin
export APP_SECURITY_ADMIN_PASSWORD='s3cret!Str0ng'
cd backend
./mvnw spring-boot:run
```

Or inline, without persisting them in the shell session:
```bash
APP_SECURITY_ADMIN_USERNAME=admin APP_SECURITY_ADMIN_PASSWORD='s3cret!Str0ng' ./mvnw spring-boot:run
```

**Docker (optional example):**
```bash
docker run -e APP_SECURITY_ADMIN_USERNAME=admin \
           -e APP_SECURITY_ADMIN_PASSWORD='s3cret!Str0ng' \
           -p 8080:8080 carexport/backend
```

### Connection examples

**Public read-only endpoints** (no credentials needed):

```bash
# Health snapshot (last scrape round)
curl http://localhost:8080/api/health

# Search
curl "http://localhost:8080/api/vehicles/search?brand=Peugeot&model=308"
```

**Admin-only endpoint** (requires HTTP Basic):

```bash
curl -u admin:s3cret!Str0ng -X POST http://localhost:8080/api/health/refresh
```

The `-u user:pass` flag adds an `Authorization: Basic base64(user:pass)` header. You can pass the header explicitly if you prefer:

```bash
curl -H "Authorization: Basic $(echo -n 'admin:s3cret!Str0ng' | base64)" \
     -X POST http://localhost:8080/api/health/refresh
```

> **Warning:** HTTP Basic only base64-encodes the credentials — it is trivially decodable and provides **no confidentiality**. Always serve the API over TLS in production (HTTPS, or a reverse proxy like nginx/Caddy); never expose it over plain HTTP, or the admin password travels readable on the wire.

### Security tests

The access rules are covered in `ScrapingHealthControllerTest` (a `@WebMvcTest` importing `SecurityConfig` and `GlobalExceptionHandler`):

| Test | Verifies |
|---|---|
| `health_returnsUnknown_whenNothingRecordedYet` | `GET /api/health` works **without** credentials (unchallenged public endpoint, `permitAll`) |
| `refresh_runsSchedulerRound_andReturnsFreshSnapshot` (`@WithMockUser(roles = "ADMIN")`) | Admin can `POST /api/health/refresh` and gets `200` |
| `refresh_returnsUnauthorized_whenNotAuthenticated` | A refresh without credentials is rejected with `401` |
| `refresh_returnsForbidden_whenAuthenticatedWithoutAdminRole` (`@WithMockUser(roles = "USER")`) | A non-admin session is rejected with `403` |
| `refresh_returnsMethodNotAllowed_whenCalledWithGet` | Browser navigation (GET) on the POST-only endpoint gets `405`, not a misleading `500` |

`@WithMockUser` swaps the real HTTP Basic authentication for a fake in-memory principal, so the tests never need real credentials — only roles matter. Run them with `mvn test` (as usual with the rest of the suite).

## Internationalization

The interface is available in English 🇬🇧, French 🇫🇷 and Arabic 🇩🇿, switchable instantly via the flag buttons in the header. Arabic also switches the document to right-to-left (`dir="rtl"`).

Translations are handled by a lightweight runtime `TranslationService` and a `translate` pipe (`src/app/i18n/translations.ts`) rather than Angular's build-time i18n — this avoids needing a separate build per locale, at the cost of type-safe translation keys (a small tradeoff for a project this size).

The backend exposes machine-readable reason codes for the customs discount tier (`ELECTRIC`, `DIESEL_NOT_ELIGIBLE`, `SMALL_ENGINE`, `LARGE_ENGINE`) rather than pre-built English sentences, so the frontend can render a fully translated, parameterized explanation in any of the three languages.

## Running locally

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

Starts on `http://localhost:8080`. In-memory H2 database by default, pre-seeded with sample data on startup — nothing persists between restarts.

Quick test: `http://localhost:8080/api/vehicles/search?brand=Peugeot&model=308`

#### Persistent storage with PostgreSQL

For data that survives restarts, run against PostgreSQL instead:

```bash
# 1. Start PostgreSQL (via Docker Compose, from the project root)
docker compose up -d

# 2. Run the backend with the postgres profile
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

Hibernate creates the schema automatically (`ddl-auto=update`) and `data-postgresql.sql` seeds it with the same sample data as the H2 setup, using `ON CONFLICT DO NOTHING` so it's safe to restart the app repeatedly without duplicate rows.

To stop and wipe the database entirely: `docker compose down -v`

### Frontend

```bash
cd frontend
npm install
ng serve
```

Starts on `http://localhost:4200`. Requires the backend to be running in parallel.

## Features

- Search by brand, model, and maximum price
- Results grouped by model, with a single "Best price" badge per model computed across all sources (ties broken by listing id), and a "Best price at <source>" hint under each group title
- Loading, error, and no-results states
- Extensible scraping architecture (Strategy pattern) to easily add new sources
- Scraping source health monitoring with an on-demand refresh endpoint
- RoRo shipping cost estimate on each vehicle card, with an editable route
- Multilingual interface (English, French, Arabic) with RTL support

## Roadmap

- Real scraping connectors (currently a demonstration example)
- Migration to a proper schema migration tool (Flyway/Liquibase) instead of Hibernate's `ddl-auto=update` for PostgreSQL
- User authentication and favorites

## License

This project is licensed under the MIT License — see the [LICENSE](./LICENSE) file for details.
