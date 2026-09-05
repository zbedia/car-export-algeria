CREATE TABLE IF NOT EXISTS vehicle_listing (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source VARCHAR(255) NOT NULL,
    external_url VARCHAR(1000) NOT NULL UNIQUE,
    brand VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL,
    vehicle_year INT NOT NULL,
    mileage_km INT,
    price DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    garage_city VARCHAR(255),
    scraped_at TIMESTAMP NOT NULL,
    fuel_type VARCHAR(20) NOT NULL,
    engine_displacement_cm3 INT,
    first_registration_date DATE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);
