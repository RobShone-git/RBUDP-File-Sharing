package group_11.datagramproject;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;

import static group_11.datagramproject.Sender.setConnection;

public class ConnectionController {

    @FXML
    private TextField addressTxt;
    @FXML
    private TextField udpIDTxt;
    @FXML
    private TextField tcpIDTxt;

    @FXML
    protected void onConnectBtn() {

        String address = addressTxt.getText();
        String tcpPort = tcpIDTxt.getText();
        String udpPort = udpIDTxt.getText();

        if (checkInput(address, tcpPort, udpPort) == true) {
            try {
                InetAddress add = InetAddress.getByName(address);
                DatagramSocket udpSocket = new DatagramSocket();
                Socket socket = new Socket(address, Integer.parseInt(tcpPort));


                setConnection(add, socket, udpSocket, Integer.parseInt(udpPort));
            } catch (IOException e) {
                // Handle connection failure
                System.out.println("Sorry, connection failed!");
            }
        }

    }

    public boolean checkInput(String address, String tcpPort, String udpPort) {
        // Regular expressions for IP address and port number validation
        String ipRegex = "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
        String portRegex = "^\\d{1,5}$";

        // Validate IP address
        if (!address.matches(ipRegex)) {
            if(!address.equals("localhost")){
                return false;
            }
        }

        // Validate TCP port
        if (!(tcpPort+"").matches(portRegex)) {
            return false;
        }

        // Validate UDP port
        if (!(udpPort+"").matches(portRegex)) {
            return false;
        }

        return true;
    }

}
