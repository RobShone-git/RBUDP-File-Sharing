package group_11.datagramproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class SenderApplication extends Application {

    private static Stage stage; // Store the Stage object

    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(SenderApplication.class.getResource("ConnectionScene.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 500);
        Image icon = new Image("icon.png");
        stage.getIcons().add(icon);
        stage.setTitle("Sender");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void setSenderScene(Socket tcpSocket, DatagramSocket udpSocket, InetAddress addr, int udpPort) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SenderApplication.class.getResource("SenderScene.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 500);

        // Get the controller instance and pass the socket to its constructor
        SenderController controller = fxmlLoader.getController();
        controller.setSocket(tcpSocket, udpSocket, addr, udpPort);

        stage.setScene(scene);
    }

    public static Stage getStage() {
        return stage;
    }
}
