package com.example.energy.percentage.repository;

import com.example.energy.percentage.model.EnergyPercentage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EnergyPercentageRepository extends JpaRepository<EnergyPercentage, Long> {

    Optional<EnergyPercentage> findByHour(LocalDateTime hour);

}
