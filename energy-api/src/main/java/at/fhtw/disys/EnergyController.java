package at.fhtw.disys;

import at.fhtw.disys.dto.CurrentHourDto;
import at.fhtw.disys.dto.HistoricDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
public class EnergyController {
        //teste mit http://localhost:8080/current-hour
    @GetMapping("/current-hour")
    public CurrentHourDto getCurrentHour() {
        LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        return new CurrentHourDto(
                now,
                143.024,
                130.101,
                14.75,
                78.54,
                7.23
        );
    }
    //teste mit http://localhost:8080/history?from=2025-05-03T10:00&to=2025-05-04T10:00
    @GetMapping("/history")
    public List<HistoricDto> getHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        // Dummy: je Stunde einen Eintrag
        long hours = java.time.Duration.between(from, to).toHours();
        return IntStream.rangeClosed(0, (int) hours)
                .mapToObj(i -> {
                    LocalDateTime ts = from.plusHours(i);
                    return new HistoricDto(ts,
                            100 + i * 1.2,
                            90 + i * 1.1,
                            10 + i * 0.1
                    );
                })
                .collect(Collectors.toList());
    }
}
