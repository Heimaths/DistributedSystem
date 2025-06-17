package at.fhtw.disys.UserInterface;

import at.fhtw.disys.UserInterface.dto.CurrentHourDto;
import at.fhtw.disys.UserInterface.dto.HistoricDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.concurrent.Task;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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

    // Einheitlicher ObjectMapper mit JavaTimeModule
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // Format für Datum/Uhrzeit in Query-Parametern
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @FXML
    private void initialize() {
        // Default-Datum/Uhrzeit
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now());
        startTimeSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0));
        endTimeSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 23));

        // Buttons an Methoden binden
        refreshButton.setOnAction(this::onRefreshButtonClick);
        showDataButton.setOnAction(this::onShowDataButtonClick);
    }

    @FXML
    private void onRefreshButtonClick(ActionEvent event) {
        Task<CurrentHourDto> task = new Task<>() {
            @Override
            protected CurrentHourDto call() throws Exception {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8084/energy/current"))
                        .GET()
                        .build();
                HttpResponse<String> response =
                        client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new RuntimeException("HTTP " + response.statusCode());
                }
                // Verwende hier den vor-konfigurierten Mapper
                return mapper.readValue(response.body(), CurrentHourDto.class);
            }
        };

        task.setOnSucceeded(e -> {
            CurrentHourDto dto = task.getValue();
            double depleted    = Math.round(dto.communityDepleted() * 100.0) / 100.0;
            double gridPortion = Math.round(dto.gridPortion()     * 100.0) / 100.0;
            communityPoolLabel.setText(
                    String.format("Community Pool: %.2f%%", depleted));
            gridPortionLabel.setText(
                    String.format("Grid Portion: %.2f%%", gridPortion));
        });

        task.setOnFailed(e -> task.getException().printStackTrace());
        new Thread(task).start();
    }

    @FXML
    private void onShowDataButtonClick(ActionEvent event) {
        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            new Alert(Alert.AlertType.WARNING,
                    "Bitte Start- und End-Datum wählen!",
                    ButtonType.OK).showAndWait();
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

        String start = URLEncoder.encode(from.format(fmt), StandardCharsets.UTF_8);
        String end   = URLEncoder.encode(to.format(fmt),   StandardCharsets.UTF_8);

        Task<List<HistoricDto>> task = new Task<>() {
            @Override
            protected List<HistoricDto> call() throws Exception {
                URI uri = URI.create(
                        "http://localhost:8084/energy/historical"
                                + "?start=" + start + "&end=" + end
                );
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .GET()
                        .build();
                HttpResponse<String> response =
                        client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new RuntimeException("HTTP " + response.statusCode());
                }
                // Auch hier den konfigurierten Mapper nutzen
                return mapper.readValue(
                        response.body(),
                        new TypeReference<List<HistoricDto>>() {}
                );
            }
        };

        task.setOnSucceeded(e -> {
            List<HistoricDto> list = task.getValue();
            double produced = 0, used = 0, grid = 0;
            for (HistoricDto d : list) {
                produced += d.communityProduced();
                used      += d.communityUsed();
                grid      += d.gridUsed();
            }
            communityProducedLabel.setText(
                    String.format("Produced: %.2f kWh", produced));
            communityUsedLabel.setText(
                    String.format("Used: %.2f kWh", used));
            gridUsedLabel.setText(
                    String.format("Grid: %.2f kWh", grid));
        });

        task.setOnFailed(e -> task.getException().printStackTrace());
        new Thread(task).start();
    }
}
