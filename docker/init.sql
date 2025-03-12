-- Erstelle die Datenbanken
CREATE DATABASE energy_usage_db;
CREATE DATABASE energy_percentage_db;

-- Verbinde mit der energy_usage_db
\c energy_usage_db

-- Erstelle die Tabelle für Energy Usage
CREATE TABLE IF NOT EXISTS energy_usage (
    id BIGSERIAL PRIMARY KEY,
    hour TIMESTAMP NOT NULL,
    community_produced DOUBLE PRECISION DEFAULT 0.0,
    community_used DOUBLE PRECISION DEFAULT 0.0,
    grid_used DOUBLE PRECISION DEFAULT 0.0,
    UNIQUE(hour)
);

-- Verbinde mit der energy_percentage_db
\c energy_percentage_db

-- Erstelle die Tabelle für Energy Percentage
CREATE TABLE IF NOT EXISTS energy_percentage (
    id BIGSERIAL PRIMARY KEY,
    hour TIMESTAMP NOT NULL,
    community_depleted DOUBLE PRECISION DEFAULT 0.0,
    grid_portion DOUBLE PRECISION DEFAULT 0.0,
    UNIQUE(hour)
);

-- Gewähre Berechtigungen
GRANT ALL PRIVILEGES ON DATABASE energy_usage_db TO disysuser;
GRANT ALL PRIVILEGES ON DATABASE energy_percentage_db TO disysuser;

-- Verbinde mit der energy_usage_db und setze Berechtigungen
\c energy_usage_db
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO disysuser;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO disysuser;

-- Verbinde mit der energy_percentage_db und setze Berechtigungen
\c energy_percentage_db
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO disysuser;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO disysuser; 