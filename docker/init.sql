    -- Erstelle die Datenbanken, falls sie nicht existieren
    DO $$
    BEGIN
       IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'energy_usage_db') THEN
          CREATE DATABASE energy_usage_db;
       END IF;
       IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'energy_percentage_db') THEN
          CREATE DATABASE energy_percentage_db;
       END IF;
    END $$;

    -- Verbinde mit der energy_usage_db
    \c energy_usage_db

    -- Erstelle die Tabelle für Energy Usage
    CREATE TABLE IF NOT EXISTS energy_usage (
        id BIGSERIAL PRIMARY KEY,
        hour TIMESTAMP NOT NULL UNIQUE,
        community_produced DOUBLE PRECISION DEFAULT 0.0,
        community_used DOUBLE PRECISION DEFAULT 0.0,
        grid_used DOUBLE PRECISION DEFAULT 0.0
    );

    -- Verbinde mit der energy_percentage_db
    \c energy_percentage_db

    -- Erstelle die Tabelle für Energy Percentage
    CREATE TABLE IF NOT EXISTS energy_percentage (
        id BIGSERIAL PRIMARY KEY,
        hour TIMESTAMP NOT NULL UNIQUE,
        community_depleted DOUBLE PRECISION DEFAULT 0.0,
        grid_portion DOUBLE PRECISION DEFAULT 0.0
    );

    -- Gewähre Berechtigungen
    \c postgres
    GRANT ALL PRIVILEGES ON DATABASE energy_usage_db TO disysuser;
    GRANT ALL PRIVILEGES ON DATABASE energy_percentage_db TO disysuser;

    \c energy_usage_db
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO disysuser;
    GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO disysuser;

    \c energy_percentage_db
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO disysuser;
    GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO disysuser;
