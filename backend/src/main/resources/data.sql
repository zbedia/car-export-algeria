-- Fuel types: ESSENCE, HYBRIDE, ELECTRIQUE, DIESEL
-- first_registration_date uses relative offsets from CURRENT_DATE so the
-- eligibility window (2 years 10 months) stays realistic whenever this runs.

-- Eligible: essence, small engine (<=1800cm3) -> 50% customs discount
INSERT INTO vehicle_listing (source, external_url, brand, model, vehicle_year, mileage_km, price, currency, garage_city, scraped_at, fuel_type, engine_displacement_cm3, first_registration_date) VALUES
('GarageX', 'https://garagex.fr/annonce/1', 'Peugeot', '308', 2025, 15000, 18500.00, 'EUR', 'Lyon', NOW(), 'ESSENCE', 1600, CAST(DATEADD('MONTH', -10, CURRENT_DATE) AS DATE));

-- Eligible: essence, large engine (>1800cm3) -> 20% customs discount
INSERT INTO vehicle_listing (source, external_url, brand, model, vehicle_year, mileage_km, price, currency, garage_city, scraped_at, fuel_type, engine_displacement_cm3, first_registration_date) VALUES
('LaCentrale', 'https://lacentrale.fr/annonce/2', 'Peugeot', '308', 2024, 22000, 17200.00, 'EUR', 'Marseille', NOW(), 'ESSENCE', 1998, CAST(DATEADD('MONTH', -20, CURRENT_DATE) AS DATE));

-- Eligible: hybrid, small engine -> 50% customs discount
INSERT INTO vehicle_listing (source, external_url, brand, model, vehicle_year, mileage_km, price, currency, garage_city, scraped_at, fuel_type, engine_displacement_cm3, first_registration_date) VALUES
('AutoScout', 'https://autoscout24.fr/annonce/3', 'Peugeot', '308', 2025, 8000, 19900.00, 'EUR', 'Paris', NOW(), 'HYBRIDE', 1500, CAST(DATEADD('MONTH', -5, CURRENT_DATE) AS DATE));

-- Eligible: electric -> 80% customs discount
INSERT INTO vehicle_listing (source, external_url, brand, model, vehicle_year, mileage_km, price, currency, garage_city, scraped_at, fuel_type, engine_displacement_cm3, first_registration_date) VALUES
('LaCentrale', 'https://lacentrale.fr/annonce/5', 'Renault', 'Clio', 2025, 5000, 15800.00, 'EUR', 'Toulouse', NOW(), 'ELECTRIQUE', NULL, CAST(DATEADD('MONTH', -8, CURRENT_DATE) AS DATE));

-- Eligible: hybrid, large engine -> 20% customs discount
INSERT INTO vehicle_listing (source, external_url, brand, model, vehicle_year, mileage_km, price, currency, garage_city, scraped_at, fuel_type, engine_displacement_cm3, first_registration_date) VALUES
('GarageX', 'https://garagex.fr/annonce/6', 'BMW', 'X5', 2025, 12000, 62000.00, 'EUR', 'Nice', NOW(), 'HYBRIDE', 2998, CAST(DATEADD('MONTH', -6, CURRENT_DATE) AS DATE));

-- NOT eligible: diesel is banned for private import regardless of age
INSERT INTO vehicle_listing (source, external_url, brand, model, vehicle_year, mileage_km, price, currency, garage_city, scraped_at, fuel_type, engine_displacement_cm3, first_registration_date) VALUES
('GarageX', 'https://garagex.fr/annonce/4', 'Renault', 'Clio', 2025, 18000, 14200.00, 'EUR', 'Lyon', NOW(), 'DIESEL', 1500, CAST(DATEADD('MONTH', -6, CURRENT_DATE) AS DATE));

-- NOT eligible: essence but older than the 2-year-10-month safety margin (34 months)
INSERT INTO vehicle_listing (source, external_url, brand, model, vehicle_year, mileage_km, price, currency, garage_city, scraped_at, fuel_type, engine_displacement_cm3, first_registration_date) VALUES
('AutoScout', 'https://autoscout24.fr/annonce/7', 'Volkswagen', 'Golf', 2022, 45000, 16500.00, 'EUR', 'Paris', NOW(), 'ESSENCE', 1500, CAST(DATEADD('MONTH', -40, CURRENT_DATE) AS DATE));
