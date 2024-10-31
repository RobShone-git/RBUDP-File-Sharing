package group_11.datagramproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import static group_11.datagramproject.Receiver.setController;

public class ReceiverApplication extends Application {
    private static Stage stage; // Store the Stage object

    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(ReceiverApplication.class.getResource("ReceiverGUI.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 520, 400);
        stage.setTitle("Receiver");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

        // Set the controller after loading the FXML file
        ReceiverController controller = fxmlLoader.getController();
        setController(controller);

        // Set an event handler to handle window close request
        stage.setOnCloseRequest(event -> {
            System.out.println("Receiver has been terminated");
            System.exit(0);
        });

    }

    public static void main(String[] args) {
        launch(args);
    }

}



