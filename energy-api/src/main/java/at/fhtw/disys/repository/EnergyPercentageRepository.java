package at.fhtw.disys.repository;

import at.fhtw.disys.model.EnergyPercentage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EnergyPercentageRepository extends JpaRepository<EnergyPercentage, Long> {

    Optional<EnergyPercentage> findByHour(LocalDateTime hour);

    List<EnergyPercentage> findAllByHourBetween(LocalDateTime start, LocalDateTime end);
}
