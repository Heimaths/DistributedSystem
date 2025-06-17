package at.fhtw.disys;

// DTOs (Data Transfer Objects) bzw. Entities, die eure Datenstruktur repräsentieren:
import at.fhtw.disys.model.EnergyPercentage;
import at.fhtw.disys.model.EnergyUsage;

// Spring Data JPA Repositories für den Datenbankzugriff
import at.fhtw.disys.repository.EnergyPercentageRepository;
import at.fhtw.disys.repository.EnergyUsageRepository;

// Annotationen, um Parameter in URL-Query-Strings als LocalDateTime zu parsen
import org.springframework.format.annotation.DateTimeFormat;

// Spring Web-Anmerkungen für REST-Controller und Mapping
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * @RestController = Spezial-Annotation aus Spring,
 *                  kombiniert @Controller + @ResponseBody:
 *                  Jede Methode liefert direkt JSON (bzw. das DTO) zurück.
 *
 * @RequestMapping("/energy") = Basispfad für alle Endpunkte in dieser Klasse.
 *                            Alle URLs fangen also mit /energy an.
 */
@RestController
@RequestMapping("/energy")
public class EnergyController {

    // Repository für die aktuelle Stunden-Prozentwerte (z. B. Community vs. Grid)
    private final EnergyPercentageRepository repository;
    // Repository für historische Verbrauchs-/Produktionsdaten
    private final EnergyUsageRepository repository2;

    /**
     * Konstruktor-Injektion (preferred in Spring):
     * - Die beiden Repositories werden von Spring beim Start automatisch
     *   reingegeben (Dependency Injection).
     * - Vorteil: leichter testbar, keine manuelle Initialisierung nötig.
     */
    public EnergyController(EnergyPercentageRepository repository,
                            EnergyUsageRepository repository2) {
        this.repository = repository;
        this.repository2 = repository2;
    }

    /**
     * GET /energy/current
     *
     * Liefert die aktuellen Prozentwerte für die jetzige Stunde
     * (z. B. 20.5% Community-Erzeugung, 79.5% Netzbezug).
     *
     * Warum synchron?
     * - Datenbank-Lesezugriff ist in der Regel schnell
     * - REST-Controller-Methoden können blockieren, Spring verwaltet
     *   intern Thread-Pools für Anfragen
     *
     * Warum LocalDateTime.truncatedTo(ChronoUnit.HOURS)?
     * - Wir wollen genau die volle Stunde (z. B. 17:00:00),
     *   damit unser Repository das passende Daten-Objekt findet.
     *
     * @return EnergyPercentage (wird automatisch in JSON serialisiert)
     *         oder null, falls kein Eintrag existiert
     */
    @GetMapping("/current")
    public EnergyPercentage getCurrentHourPercentage() {
        // Bestimme "jetzt" abgerundet auf volle Stunde
        LocalDateTime currentHour = LocalDateTime.now()
                .truncatedTo(ChronoUnit.HOURS);

        // Suche im Repository nach dem passenden Objekt
        // findByHour(...) gibt ein Optional zurück, wir geben null weiter,
        // wenn nichts gefunden wird (-> 204 No Content oder 200 null im JSON).
        return repository.findByHour(currentHour)
                .orElse(null);
    }

    /**
     * GET /energy/historical?start=...&end=...
     *
     * Liefert alle Verbrauchs-/Produktionsdaten zwischen zwei Zeitpunkten.
     *
     * @RequestParam = holt die Query-Parameter "start" und "end" aus der URL.
     * @DateTimeFormat = sagt Spring, in welchem ISO-Format das Datum erwartet wird.
     *
     * Beispiel-URL:
     *   /energy/historical?start=2025-06-17T00:00&end=2025-06-17T23:00
     *
     * @return Liste von EnergyUsage-Objekten, die Spring automatisch
     *         in ein JSON-Array umwandelt.
     */
    @GetMapping("/historical")
    public List<EnergyUsage> getHistory(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end) {

        // Übergibt die beiden Zeiten an das Repository, das
        // findAllByHourBetween(start, end) implementiert hat.
        return repository2.findAllByHourBetween(start, end);
    }
}
