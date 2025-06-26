package com.example.energy.percentage.service;

import com.example.energy.percentage.model.EnergyUsage;
import com.example.energy.percentage.repository.EnergyUsageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

// Service-Layer, der Verbrauchs-/Erzeugungsdaten über JPA holt. UsageDataService ist die Schnittstelle zwischen Business-Logik und Datenbank
@Service
public class UsageDataService {
    private final EnergyUsageRepository usageRepository;

    // Bindet das EnergyUsageRepository, mit dem Datenbankzugriffe auf Verbrauchsdaten erfolgen.
    public UsageDataService(EnergyUsageRepository usageRepository) {
        this.usageRepository = usageRepository;
    }

    // Liefert Optional<EnergyUsage> für die gegebene volle Stunde.
    public Optional<EnergyUsage> getUsageData(LocalDateTime dateTime) {
        LocalDateTime hour = dateTime.truncatedTo(ChronoUnit.HOURS);
        // Fragt in der Datenbank nach einem passenden EnergyUsage-Eintrag über findByHour(hour)
        return usageRepository.findByHour(hour);
    }
}

/*
PercentageService ruft getUsageData(...) auf, um zu prüfen, ob es Verbrauchsdaten für eine bestimmte Stunde gibt.
Falls ja: werden daraus Prozentwerte berechnet und gespeichert.
Falls nein: wird eine Log-Warnung ausgegeben, aber nichts geschrieben
*/
