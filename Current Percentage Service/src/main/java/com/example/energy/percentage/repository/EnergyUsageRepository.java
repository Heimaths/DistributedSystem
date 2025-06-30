package com.example.energy.percentage.repository;

import com.example.energy.percentage.model.EnergyUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EnergyUsageRepository extends JpaRepository<EnergyUsage, Long> { // stellt automatisch CRUD-Methoden zur Verfügung, ohne SQL-Statements

    Optional<EnergyUsage> findByHour(LocalDateTime hour);

    List<EnergyUsage> findAllByHourBetween(LocalDateTime start, LocalDateTime end);

}
