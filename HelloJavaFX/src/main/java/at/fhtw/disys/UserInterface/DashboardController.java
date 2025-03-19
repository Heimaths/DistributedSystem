package at.fhtw.disys.UserInterface;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

public class DashboardController {

    @FXML private Label communityPoolLabel;
    @FXML private Label gridPortionLabel;
    @FXML private Button refreshButton;
    @FXML private Button startDateButton;
    @FXML private Button endDateButton;
    @FXML private Button showDataButton;
    @FXML private Label communityProducedLabel;
    @FXML private Label communityUsedLabel;
    @FXML private Label gridUsedLabel;

    private LocalDate startDate;
    private LocalDate endDate;

    @FXML
    private void initialize() {
        if (startDateButton != null && endDateButton != null) {
            startDateButton.setOnAction(event -> selectDate(true));
            endDateButton.setOnAction(event -> selectDate(false));
        } else {
            System.out.println("Error: FXML elements not injected properly.");
        }
    }

    private void selectDate(boolean isStart) {
        Stage dateStage = new Stage();
        DatePicker datePicker = new DatePicker();

        Button confirmButton = new Button("OK");
        confirmButton.setOnAction(event -> {
            if (isStart) {
                startDate = datePicker.getValue();
                startDateButton.setText("Start: " + startDate);
            } else {
                endDate = datePicker.getValue();
                endDateButton.setText("End: " + endDate);
            }
            dateStage.close();
        });

        VBox vbox = new VBox(10, datePicker, confirmButton);
        dateStage.setScene(new Scene(vbox, 250, 150));
        dateStage.setTitle(isStart ? "Select Start Date" : "Select End Date");
        dateStage.show();
    }
}
