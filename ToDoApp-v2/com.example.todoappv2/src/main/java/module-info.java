module com.example.comexampletodoappv2 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.comexampletodoappv2 to javafx.fxml;
    exports com.example.comexampletodoappv2;
}