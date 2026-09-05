-- Fuel types: ESSENCE, HYBRIDE, ELECTRIQUE, DIESEL
-- first_registration_date uses relative offsets from CURRENT_DATE so the
-- eligibility window (2 years 10 months) stays realistic whenever this runs.
-- PostgreSQL-specific syntax (INTERVAL), as opposed to H2's DATEADD used
-- in the default data.sql.

INSERT INTO vehicle_listing (source, external_url, brand, model, vehicle_year, mileage_km, price, currency, garage_city, scraped_at, fuel_type, engine_displacement_cm3, first_registration_date, version) VALUES
('GarageX', 'https://garagex.fr/annonce/1', 'Peugeot', '308', 2025, 15000, 18500.00, 'EUR', 'Lyon', NOW(), 'ESSENCE', 1600, (CURRENT_DATE - INTERVAL '10 months')::date, 0)
ON CONFLICT (external_url) DO NOTHING;

INSERT INTO vehicle_listing (source, external_url, brand, model, vehicle_year, mileage_km, price, currency, garage_city, scraped_at, fuel_type, engine_displacement_cm3, first_registration_date, version) VALUES
('LaCentrale', 'https://lacentrale.fr/annonce/2', 'Peugeot', '308', 2024, 22000, 17200.00, 'EUR', 'Marseille', NOW(), 'ESSENCE', 1998, (CURRENT_DATE - INTERVAL '20 months')::date, 0)
ON CONFLICT (external_url) DO NOTHING;

INSERT INTO vehicle_listing (source, external_url, brand, model, vehicle_year, mileage_km, price, currency, garage_city, scraped_at, fuel_type, engine_displacement_cm3, first_registration_date, version) VALUES
('AutoScout', 'https://autoscout24.fr/annonce/3', 'Peugeot', '308', 2025, 8000, 19900.00, 'EUR', 'Paris', NOW(), 'HYBRIDE', 1500, (CURRENT_DATE - INTERVAL '5 months')::date, 0)
ON CONFLICT (external_url) DO NOTHING;

INSERT INTO vehicle_listing (source, external_url, brand, model, vehicle_year, mileage_km, price, currency, garage_city, scraped_at, fuel_type, engine_displacement_cm3, first_registration_date, version) VALUES
('LaCentrale', 'https://lacentrale.fr/annonce/5', 'Renault', 'Clio', 2025, 5000, 15800.00, 'EUR', 'Toulouse', NOW(), 'ELECTRIQUE', NULL, (CURRENT_DATE - INTERVAL '8 months')::date, 0)
ON CONFLICT (external_url) DO NOTHING;

INSERT INTO vehicle_listing (source, external_url, brand, model, vehicle_year, mileage_km, price, currency, garage_city, scraped_at, fuel_type, engine_displacement_cm3, first_registration_date, version) VALUES
('GarageX', 'https://garagex.fr/annonce/6', 'BMW', 'X5', 2025, 12000, 62000.00, 'EUR', 'Nice', NOW(), 'HYBRIDE', 2998, (CURRENT_DATE - INTERVAL '6 months')::date, 0)
ON CONFLICT (external_url) DO NOTHING;

INSERT INTO vehicle_listing (source, external_url, brand, model, vehicle_year, mileage_km, price, currency, garage_city, scraped_at, fuel_type, engine_displacement_cm3, first_registration_date, version) VALUES
('GarageX', 'https://garagex.fr/annonce/4', 'Renault', 'Clio', 2025, 18000, 14200.00, 'EUR', 'Lyon', NOW(), 'DIESEL', 1500, (CURRENT_DATE - INTERVAL '6 months')::date, 0)
ON CONFLICT (external_url) DO NOTHING;

INSERT INTO vehicle_listing (source, external_url, brand, model, vehicle_year, mileage_km, price, currency, garage_city, scraped_at, fuel_type, engine_displacement_cm3, first_registration_date, version) VALUES
('AutoScout', 'https://autoscout24.fr/annonce/7', 'Volkswagen', 'Golf', 2022, 45000, 16500.00, 'EUR', 'Paris', NOW(), 'ESSENCE', 1500, (CURRENT_DATE - INTERVAL '40 months')::date, 0)
ON CONFLICT (external_url) DO NOTHING;
