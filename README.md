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
| **Backend** | Java 21, Spring Boot 3.3, Spring Data JPA, H2, Jsoup |
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

Starts on `http://localhost:8080`. In-memory H2 database, pre-seeded with sample data on startup.

Quick test: `http://localhost:8080/api/vehicles/search?brand=Peugeot&model=308`

### Frontend

```bash
cd frontend
npm install
ng serve
```

Starts on `http://localhost:4200`. Requires the backend to be running in parallel.

## Features

- Search by brand, model, and maximum price
- Results grouped by model, with a "Best price" badge on the cheapest vehicle in each group
- Loading, error, and no-results states
- Extensible scraping architecture (Strategy pattern) to easily add new sources
- RoRo shipping cost estimate on each vehicle card, with an editable route
- Multilingual interface (English, French, Arabic) with RTL support

## Roadmap

- Real scraping connectors (currently a demonstration example)
- Migration to PostgreSQL for production persistence
- Advanced filters (mileage, city, fuel type) and pagination
- Support for customs rules specific to export to Algeria
- User authentication and favorites

## License

This project is licensed under the MIT License — see the [LICENSE](./LICENSE) file for details.
