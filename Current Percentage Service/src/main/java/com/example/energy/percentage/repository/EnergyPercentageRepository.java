package com.example.energy.percentage.repository;

import com.example.energy.percentage.model.EnergyPercentage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EnergyPercentageRepository extends JpaRepository<EnergyPercentage, Long> {
    
    @Query("SELECT e FROM EnergyPercentage e WHERE e.hour = ?1")
    Optional<EnergyPercentage> findByHour(LocalDateTime hour);
} 