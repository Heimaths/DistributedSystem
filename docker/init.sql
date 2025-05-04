-- init.sql

-- Diese Datei wird ausgeführt, wenn der Container startet und sich auf "postgres" verbindet.
-- Daher legen wir hier die DB an und initialisieren NUR "energy_usage_db".

CREATE DATABASE energy_usage_db;

-- WICHTIG: danach wird PostgreSQL automatisch beendet. Die folgenden Zeilen wären nur sinnvoll,
-- wenn du dich nachträglich verbindest. Daher gehört das in eine zweite Datei oder in deinen App-Code.

-- Optional: Rechte für Benutzer setzen (in der DB energy_usage_db)
-- Das geht nicht direkt hier, weil du dafür verbunden sein musst.

-- HINWEIS: Tabelle kann in einem separaten Skript erstellt werden, das beim Start von energy_usage_db ausgeführt wird.
