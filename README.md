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
| **Backend** | Java 17, Spring Boot 3.3, Spring Data JPA, H2, Jsoup |
| **Frontend** | Angular 18 (standalone components), TypeScript, RxJS |

## How it works

User searches only query data already collected in the database — no real-time scraping. A scheduled job periodically refreshes the data in the background, which keeps API responses fast and reliable regardless of the external sites' availability.

## Algerian import regulations

The backend enforces the import rules for private individuals (Decret executif n. 23-74 and the finance law) directly at the search level — non-eligible vehicles never appear in the results:

- **3-year age rule**: computed to the day between first registration and today, with a 2-year-10-month safety margin applied instead of the full 3 years, to absorb delays before customs clearance.
- **Allowed fuel types only**: Essence ⛽, Hybrid 🔋 and Electric ⚡. Diesel 🚫 is strictly banned for private import of vehicles under 3 years old.
- **Customs duty reduction**, shown for each result:
    - Electric: -80%
    - Essence/Hybrid ≤ 1800 cm³: -50%
    - Essence/Hybrid > 1800 cm³: -20%

This logic lives in `ImportEligibilityService`, kept separate from the search logic so it can evolve independently if the regulation changes.

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

## Roadmap

- Real scraping connectors (currently a demonstration example)
- Migration to PostgreSQL for production persistence
- Advanced filters (mileage, city, fuel type) and pagination
- Support for customs rules specific to export to Algeria
- User authentication and favorites

## License

This project is licensed under the MIT License — see the [LICENSE](./LICENSE) file for details.

