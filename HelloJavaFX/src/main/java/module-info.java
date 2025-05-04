module at.fhtw.disys.UserInterface {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;


    opens at.fhtw.disys.UserInterface to javafx.fxml;
    exports at.fhtw.disys.UserInterface;
}