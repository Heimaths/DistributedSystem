package at.fhtw.disys.UserInterface;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import at.fhtw.disys.UserInterface.dto.CurrentHourDto;
import at.fhtw.disys.UserInterface.dto.HistoricDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;



public class DashboardController {
    @FXML private Label communityPoolLabel;
    @FXML private Label gridPortionLabel;
    @FXML private Button refreshButton;
    @FXML private DatePicker startDatePicker;
    @FXML private Spinner<Integer> startTimeSpinner;
    @FXML private DatePicker endDatePicker;
    @FXML private Spinner<Integer> endTimeSpinner;
    @FXML private Button showDataButton;
    @FXML private Label communityProducedLabel;
    @FXML private Label communityUsedLabel;
    @FXML private Label gridUsedLabel;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME;

    @FXML
    private void initialize() {
        mapper.registerModule(new JavaTimeModule());
        // Spinner initialisieren
        startTimeSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0));
        endTimeSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 23));

        refreshButton.setOnAction(ev -> fetchCurrentHour());
        showDataButton.setOnAction(ev -> fetchHistory());
    }

    private void fetchCurrentHour() {
        var req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8084/energy/current"))
                .GET().build();
        http.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::updateCurrentHour)
                .exceptionally(ex -> { ex.printStackTrace(); return null; });
    }

    private void updateCurrentHour(String json) {
        try {
            var dto = mapper.readValue(json, CurrentHourDto.class);
            Platform.runLater(() -> {
                double rounded = Math.round(dto.communityDepleted() * 100.0) / 100.0;
                double rounded2 = Math.round(dto.gridPortion() * 100.0) / 100.0;

                communityPoolLabel.setText("Community Pool " + String.format("%.2f", rounded) + "%");
                gridPortionLabel.setText("Grid Portion  " + String.format("%.2f", rounded2) + "%");

                communityProducedLabel.setText("Community produced  " + dto.communityProduced());
                communityUsedLabel.setText("Community used  " + dto.communityUsed());
                gridUsedLabel.setText("Grid used  " + dto.gridUsed());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchHistory() {
        var from = LocalDateTime.of(
                startDatePicker.getValue(),
                java.time.LocalTime.of(startTimeSpinner.getValue(), 0)
        );
        var to = LocalDateTime.of(
                endDatePicker.getValue(),
                java.time.LocalTime.of(endTimeSpinner.getValue(), 0)
        );
        String uri = String.format(
                "http://localhost:8080/api/history?from=%s&to=%s",
                from.format(fmt), to.format(fmt)
        );
        var req = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET().build();
        http.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::showHistoryInConsole)
                .exceptionally(ex -> { ex.printStackTrace(); return null; });
    }

    private void showHistoryInConsole(String json) {
        try {
            List<HistoricDto> list = mapper.readValue(
                    json, new TypeReference<>() {}
            );
            // Beispiel: einfach mal in der Konsole
            list.forEach(d -> System.out.println(d));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
