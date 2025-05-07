module at.fhtw.disys.UserInterface {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.databind;


    opens at.fhtw.disys.UserInterface to javafx.fxml;
    opens at.fhtw.disys.UserInterface.dto to  com.fasterxml.jackson.databind;
    exports at.fhtw.disys.UserInterface;
}