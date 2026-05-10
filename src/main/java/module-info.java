module org.example {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.example.dice3d to javafx.fxml;
    exports org.example.dice3d;
}
