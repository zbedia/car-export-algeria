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
    scraped_at TIMESTAMP NOT NULL
);
