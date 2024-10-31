package group_11.datagramproject;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import static group_11.datagramproject.SenderApplication.setSenderScene;

public class Sender {

    public static InetAddress address;

    public static DatagramSocket udpSocket;

    public static Socket tcpSocket;

    public static void main(String[] args) {
        SenderApplication.launch(SenderApplication.class, args);
    }

    public static void setConnection(InetAddress add, Socket tcp, DatagramSocket udp, int udpPort) throws IOException {
        address = add;
        tcpSocket = tcp;
        udpSocket = udp;

        setSenderScene(tcpSocket, udpSocket, add, udpPort);
    }

}
