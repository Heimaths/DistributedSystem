package com.example.energy.percentage.service;

import com.example.energy.percentage.model.EnergyUsage;
import com.example.energy.percentage.repository.EnergyUsageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Service-Layer, der Verbrauchs-/Erzeugungsdaten über JPA holt.
 */

@Service
public class UsageDataService {
    private final EnergyUsageRepository usageRepository;

    public UsageDataService(EnergyUsageRepository usageRepository) {
        this.usageRepository = usageRepository;
    }

    /**
     * Liefert Optional<EnergyUsage> für die gegebene volle Stunde.
     */
    public Optional<EnergyUsage> getUsageData(LocalDateTime dateTime) {
        LocalDateTime hour = dateTime.truncatedTo(ChronoUnit.HOURS);
        return usageRepository.findByHour(hour);
    }
}
