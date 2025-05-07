package at.fhtw.disys.repository;

import at.fhtw.disys.model.EnergyUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EnergyUsageRepository extends JpaRepository<EnergyUsage, Long> {

    Optional<EnergyUsage> findByHour(LocalDateTime hour);

    List<EnergyUsage> findAllByHourBetween(LocalDateTime start, LocalDateTime end);

}
