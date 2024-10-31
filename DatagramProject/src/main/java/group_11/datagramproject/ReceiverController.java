package group_11.datagramproject;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

public class ReceiverController {

    @FXML
    private VBox filesTCP;

    @FXML
    private VBox filesUDP;

    @FXML
    private ProgressBar pgrBarTCP;

    @FXML
    private ProgressBar pgrBarUDP;

    public void addTCPFile(String filename) {
        Platform.runLater(() -> {
            Label label = new Label(filename);
            filesTCP.getChildren().add(label);
        });
    }

    public void setTCPProgress(double progress) {
        Platform.runLater(() -> {
            pgrBarTCP.setProgress(progress);
        });
    }

    public void setUDPProgress(double progress) {
        Platform.runLater(() -> {
            pgrBarUDP.setProgress(progress);
        });
    }

    public void addUDPFile(String filename) {
        Platform.runLater(() -> {
            Label label = new Label(filename);
            filesUDP.getChildren().add(label);
        });
    }


}
