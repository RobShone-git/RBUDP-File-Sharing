package group_11.datagramproject;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.util.*;

public class SenderController {

    public Button btnSendTCP;
    public Button btnSendUDP;
    public Slider packetSizeSld;
    public Slider blastLengthSld;
    private File selectedFile;
    public Socket tcpSocket;
    public DatagramSocket udpSocket;
    public InetAddress address;
    public int udpPort;
    public DataOutputStream dataOutputStream;
    public  ObjectInputStream objectInputStream;

    public  ObjectOutputStream listOutputStream;

    private int count = 0;

    @FXML
    private Label filePath;
    @FXML
    private TextField txtBlastSliderAmount;
    @FXML
    private TextField txtPacketSliderAmount;

    @FXML
    private void initialize() {
        // Set up a listener for slider value changes
        packetSizeSld.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                txtPacketSliderAmount.setText(String.format("%d", newValue.intValue()));
            }
        });
        blastLengthSld.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                txtBlastSliderAmount.setText(String.format("%d", newValue.intValue()));
            }
        });
        txtBlastSliderAmount.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    if (Integer.parseInt(newValue) > 10000) {
                        blastLengthSld.setValue(10000);
                        txtBlastSliderAmount.setText(10000 + "");
                    } else if (Integer.parseInt(newValue) < 0) {
                        blastLengthSld.setValue(0);
                        txtBlastSliderAmount.setText(0 + "");
                    } else {
                        blastLengthSld.setValue(Integer.parseInt(newValue));
                    }

                }catch (NumberFormatException e) {
                    if (newValue.isEmpty()) {
                        txtBlastSliderAmount.setText(0+"");
                    } else {
                        txtBlastSliderAmount.setText((int)blastLengthSld.getValue()+"");
                    }

                }

            }
        });
        txtPacketSliderAmount.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    if (Integer.parseInt(newValue) > 64000) {
                        packetSizeSld.setValue(64000);
                        txtPacketSliderAmount.setText(64000 + "");
                    } else if (Integer.parseInt(newValue) < 0) {
                        packetSizeSld.setValue(0);
                        txtPacketSliderAmount.setText(0 + "");

                    } else {
                        packetSizeSld.setValue(Integer.parseInt(newValue));
                    }
                }catch (NumberFormatException e) {
                    if (newValue.isEmpty()) {
                        txtPacketSliderAmount.setText(0+"");
                    } else {
                        txtPacketSliderAmount.setText((int)packetSizeSld.getValue()+"");
                    }

                }
            }
        });
    }

    public void setSocket(Socket tcp, DatagramSocket udp, InetAddress addr, int udpPort) throws IOException {
        this.tcpSocket = tcp;
        this.udpSocket = udp;
        this.address = addr;
        this.udpPort = udpPort;
        dataOutputStream = new DataOutputStream(tcpSocket.getOutputStream());
        objectInputStream = new ObjectInputStream(tcpSocket.getInputStream());
        listOutputStream = new ObjectOutputStream(dataOutputStream);

    }

    @FXML
    protected void onSelectBtn() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a file");
        Stage stage = SenderApplication.getStage();
        selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            filePath.setText(selectedFile.getName());
            filePath.setVisible(true);
            btnSendUDP.setDisable(false);
            btnSendTCP.setDisable(false);
        }
    }

    @FXML
    protected void onSendTCPBtn() throws IOException {

        if (selectedFile != null) {
            try {
                //DataOutputStream dataOutputStream = new DataOutputStream(tcpSocket.getOutputStream());
                FileInputStream fileInputStream = new FileInputStream(selectedFile.getAbsolutePath());

                // This tells the receiver to expect TCP
                dataOutputStream.writeInt(0);
                dataOutputStream.flush();

                String fileName = selectedFile.getName();
                byte[] fileNameBytes = fileName.getBytes();

                byte[] fileContentBytes = new byte[(int) selectedFile.length()];
                fileInputStream.read(fileContentBytes);

                dataOutputStream.writeInt(fileNameBytes.length);
                dataOutputStream.write(fileNameBytes);

                dataOutputStream.writeInt(fileContentBytes.length);
                dataOutputStream.write(fileContentBytes);

            } catch (IOException e) {
                System.out.println("Connection to Receiver has been terminated");
                System.exit(0);
            }

        }

    }

    @FXML
    protected void onSendUDPBtn() throws ClassNotFoundException {

        // Send packets over UDP
        int packetSize = (int) packetSizeSld.getValue();
        int blastSize = (int) blastLengthSld.getValue();

        if (selectedFile != null && packetSize != 0 && blastSize != 0) {

            try {
                // This tells the receiver to expect UDP
                //DataOutputStream dataOutputStream = new DataOutputStream(tcpSocket.getOutputStream());
                dataOutputStream.writeInt(1);
                dataOutputStream.flush();

                // Send over the name of the file
                String fileName = selectedFile.getName();
                byte[] fileNameBytes = fileName.getBytes();
                dataOutputStream.writeInt(fileNameBytes.length);
                dataOutputStream.write(fileNameBytes);

                // Read the contents of the selected file into a byte array
                byte[] fileData = Files.readAllBytes(selectedFile.toPath());
                // Send over the length of the file
                dataOutputStream.writeInt(fileData.length);

                // Create a map to store packet data indexed by sequence number
                Map<Integer, DatagramPacket> packetMap = new TreeMap<>();


//            int packetSize = 50000; // Packet size
//            int blastSize = 200; // Number of packets sent per blast
                int totalPackets = (int) Math.ceil((double) fileData.length / packetSize);

                // This tells the receiver to expect x amount of packets in total
                dataOutputStream.writeInt(totalPackets);
                dataOutputStream.flush();

                // Loop to create packets and store them in map
                RBUDPDatagramPacket data = null;
                for (int i = 0; i < totalPackets; i++) {
                    int offset = i * packetSize;
                    int length = Math.min(packetSize, fileData.length - offset);

                    byte[] packetData = Arrays.copyOfRange(fileData, offset, offset + length);

                    // Create an RBUDPDatagramPacket with the file data
                    data = new RBUDPDatagramPacket(packetData);

                    // Serialize the packet
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                    objectOutputStream.writeObject(data);
                    objectOutputStream.close();

                    // Get the serialized data
                    byte[] sendData = outputStream.toByteArray();

                    // Create a DatagramPacket with the serialized data
                    DatagramPacket packet = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), udpPort);

                    // Send the DatagramPacket through the DatagramSocket
                    //udpSocket.send(packet);

                    // Add the sequence number to the list of sent packets
                    //sentPacketSequenceNumbers.add(data.getSequenceNumber());
                    packetMap.put(data.getSequenceNumber(), packet);
                }

                System.out.println("map of packets = " + packetMap.keySet());

                ;
                int loop = (int) Math.ceil((double) totalPackets / blastSize);



                for (int i = 0; i < loop; i++) {

                    // Create a list to hold the sent packet sequence numbers
                    List<Integer> sentPacketSequenceNumbers = new ArrayList<>();

                    for (int x = 0; x < blastSize; x++) {
                        if (packetMap.containsKey(count)) {
                            System.out.println("send key = " + count);
                            udpSocket.send(packetMap.get(count));
                            // Add the sequence number to the list of sent packets
                            sentPacketSequenceNumbers.add(count);
                            count++;
                        } else {
                            break;
                        }
                    }
                    System.out.println("Send signal -1");
                    // This tells the receiver that all the packets from blast have been sent from the sender side
                    dataOutputStream.writeInt(-1);
                    dataOutputStream.flush();

                    // Sends list so sender can check if its missing files
                    listOutputStream.writeObject(sentPacketSequenceNumbers);
                    System.out.println(sentPacketSequenceNumbers);

                    // Receive signal to show if there's missing packets
                    //ObjectInputStream objectInputStream = new ObjectInputStream(tcpSocket.getInputStream());
                    //Object receivedObject = objectInputStream.readObject();
                    DataInputStream inputStream = new DataInputStream(tcpSocket.getInputStream());

                    boolean receivedValue = inputStream.readBoolean();
                    System.out.println(receivedValue);

                    if(receivedValue) {
                        System.out.println("No missing packets");

                    } else {
                        System.out.println("There are missing packets");





                        // listens until all missing packets from this blast have been received
                        while (true) {

                            List<Integer> missingPacketsList = (List<Integer>) objectInputStream.readObject();
                            System.out.println(missingPacketsList);

                            sentPacketSequenceNumbers.clear();

                            for (int num : missingPacketsList) {
                                udpSocket.send(packetMap.get(num));
                                System.out.println("send this missing packet = " + num);
                                sentPacketSequenceNumbers.add(num);
                            }

                            dataOutputStream.writeInt(-1);
                            dataOutputStream.flush();

                            listOutputStream.writeObject(sentPacketSequenceNumbers);
                            System.out.println(sentPacketSequenceNumbers);

                            // DataInputStream inputStream = new DataInputStream(tcpSocket.getInputStream());

                            receivedValue = inputStream.readBoolean();
                            System.out.println(receivedValue);

                            if (receivedValue) {
                                System.out.println("no more missing packets from this blast");
                                break;
                            }

                        }
                    }
                }

                System.out.println("File sent");

            } catch (IOException e) {
                System.out.println("Connection to Receiver has been terminated");
                System.exit(0);
            }

        }
    }

}

class RBUDPDatagramPacket implements Serializable{
    private static int sequenceNumber = 0;

    private int packetSequenceNumber;
    private byte[] data;

    public RBUDPDatagramPacket(byte[] data) {
        this.packetSequenceNumber = sequenceNumber++;
        this.data = data;
    }

    public int getSequenceNumber() {
        return packetSequenceNumber;
    }

    public byte[] getData() {
        return data;
    }

}
