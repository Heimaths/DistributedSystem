module at.fhtw.disys.UserInterface {
    requires javafx.controls;
    requires javafx.fxml;


    opens at.fhtw.disys.UserInterface to javafx.fxml;
    exports at.fhtw.disys.UserInterface;
}