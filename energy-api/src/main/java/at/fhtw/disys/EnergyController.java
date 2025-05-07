
package at.fhtw.disys;

import at.fhtw.disys.model.EnergyPercentage;
import at.fhtw.disys.model.EnergyUsage;
import at.fhtw.disys.repository.EnergyPercentageRepository;
import at.fhtw.disys.repository.EnergyUsageRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/energy")
public class EnergyController {

    private final EnergyPercentageRepository repository;
    private final EnergyUsageRepository repository2;

    public EnergyController(EnergyPercentageRepository repository, EnergyUsageRepository repository2) {
        this.repository = repository;
        this.repository2 = repository2;
    }

    @GetMapping("/current")
    public EnergyPercentage getCurrentHourPercentage() {
        LocalDateTime currentHour = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
        return repository.findByHour(currentHour)
                .orElse(null);
    }

    @GetMapping("/historical")
    public List<EnergyUsage> getHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return repository2.findAllByHourBetween(start, end);
    }
}
