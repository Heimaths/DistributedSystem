package at.fhtw.disys.UserInterface;

import at.fhtw.disys.UserInterface.dto.CurrentHourDto;
import at.fhtw.disys.UserInterface.dto.HistoricDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    @FXML private TextArea historyTextArea;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @FXML
    private void initialize() {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Default dates to today to avoid nulls
        startDatePicker.setValue(java.time.LocalDate.now());
        endDatePicker.setValue(java.time.LocalDate.now());

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
                double depleted = Math.round(dto.communityDepleted() * 100.0) / 100.0;
                double gridPortionVal = Math.round(dto.gridPortion() * 100.0) / 100.0;

                communityPoolLabel.setText(String.format("Community Pool: %.2f%%", depleted));
                gridPortionLabel.setText(String.format("Grid Portion: %.2f%%", gridPortionVal));


            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchHistory() {
        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            Platform.runLater(() -> {
                new Alert(Alert.AlertType.WARNING, "Bitte Start- und End-Datum wählen!", ButtonType.OK)
                        .showAndWait();
            });
            return;
        }
        LocalDateTime from = LocalDateTime.of(
                startDatePicker.getValue(),
                LocalTime.of(startTimeSpinner.getValue(), 0)
        );
        LocalDateTime to = LocalDateTime.of(
                endDatePicker.getValue(),
                LocalTime.of(endTimeSpinner.getValue(), 0)
        );
        String uri = String.format(
                "http://localhost:8084/energy/historical?start=%s&end=%s",
                from.format(fmt), to.format(fmt)
        );
        var req = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET().build();
        http.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::displayHistory)
                .exceptionally(ex -> { ex.printStackTrace(); return null; });
    }

    private void displayHistory(String json) {
        try {
            List<HistoricDto> list = mapper.readValue(
                    json, new TypeReference<List<HistoricDto>>() {});
            Platform.runLater(() -> {
                // Kumulierte Summen berechnen
                double totalProduced = 0.0;
                double totalUsed = 0.0;
                double totalGrid = 0.0;
                for (HistoricDto d : list) {
                    totalProduced += d.communityProduced();
                    totalUsed += d.communityUsed();
                    totalGrid += d.gridUsed();
                }

                // Summen in Labels anzeigen
                communityProducedLabel.setText(String.format("Produced: %.2f kWh", totalProduced));
                communityUsedLabel.setText(String.format("Used: %.2f kWh", totalUsed));
                gridUsedLabel.setText(String.format("Grid: %.2f kWh", totalGrid));


            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
