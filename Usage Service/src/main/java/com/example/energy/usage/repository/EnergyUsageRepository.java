package com.example.energy.usage.repository;

import com.example.energy.usage.model.EnergyUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EnergyUsageRepository extends JpaRepository<EnergyUsage, Long> {
    
    @Query("SELECT e FROM EnergyUsage e WHERE e.hour = ?1")
    Optional<EnergyUsage> findByHour(LocalDateTime hour);
} 