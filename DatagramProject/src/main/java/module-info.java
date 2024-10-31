module group_11.datagramproject {
    requires javafx.controls;
    requires javafx.fxml;


    opens group_11.datagramproject to javafx.fxml;
    exports group_11.datagramproject;
}