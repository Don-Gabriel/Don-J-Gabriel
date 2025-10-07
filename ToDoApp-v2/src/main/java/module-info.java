module com.example.todoappv2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;  // for system tray notifications
    requires java.sql;      // <-- add this for JDBC

    opens com.example.todoappv2 to javafx.fxml;
    exports com.example.todoappv2;
}
