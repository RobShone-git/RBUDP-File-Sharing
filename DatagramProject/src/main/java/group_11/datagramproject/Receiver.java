package group_11.datagramproject;

import java.io.*;
import java.net.*;
import java.util.*;

public class Receiver {

    public static ReceiverController controller;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // Create a server socket to listen on port 12345
        ServerSocket serverSocket = new ServerSocket(12345);

        // Run the GUI on a separate thread
        Thread guiThread = new Thread(() -> {
            ReceiverApplication.launch(ReceiverApplication.class, args);
        });
        guiThread.setDaemon(true); // Set the GUI thread as a daemon thread to allow termination when the main thread exits
        guiThread.start();


        Socket clientSocket = serverSocket.accept();
        System.out.println("Sender Connected!");

        // Create input stream to receive data from client
        DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());

        // Create output stream to send data to client
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());

        ObjectInputStream listInputStream = new ObjectInputStream(inputStream);

        // Create a DatagramSocket to receive datagram packets over UDP
        DatagramSocket udpSocket = new DatagramSocket(8888); // Choose an appropriate UDP port

        while (true) {

            try {
                // 0 means to handle TCP and 1 means to handle UDP
                int protocol = inputStream.readInt();

                if (protocol == 0) {
                    if (handleTCP(inputStream) == false) {
                        break;
                    }
                } else if (protocol == 1) {
                    if (handleUDP(udpSocket, inputStream, clientSocket, listInputStream, objectOutputStream) == false) {
                        break;
                    }
                } else {
                    // Handle unexpected protocol value
                    System.out.println("Received unexpected protocol value: " + protocol);
                    break;
                }
            } catch (IOException e) {
                System.out.println("Sender has disconnected!");
                System.exit(0);
            }


        }



    }

    public static void setController(ReceiverController con) {
        controller = con;
    }

    public static boolean handleUDP(DatagramSocket udpSocket, DataInputStream inputStream,  Socket clientSocket, ObjectInputStream listInputStream, ObjectOutputStream objectOutputStream) throws ClassNotFoundException {

        // Create a map to store received packet data indexed by sequence number
        Map<Integer, byte[]> receivedPacketData = new TreeMap<>();

        try {
            // get name of file
            int fileNameLength = inputStream.readInt();
            byte[] fileNameBytes = new byte[fileNameLength];
            inputStream.readFully(fileNameBytes);

            // Convert the filename bytes to a string
            String fileName = new String(fileNameBytes, "UTF-8");

            // Set the total amount of bytes of the file
            int expectedTotalFileSize = inputStream.readInt();

            // Set the total amount of packets that should be received
            int totalNumberPackets = inputStream.readInt();

            // Set a timeout value for the DatagramSocket (e.g., 5000 milliseconds)
            udpSocket.setSoTimeout(1000); // Timeout of 2 seconds

            while (receivedPacketData.size() != totalNumberPackets) {
                try {
                    // Receive datagram packets over UDP
                    byte[] receiveData = new byte[udpSocket.getReceiveBufferSize()];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    // Receive a packet with the specified timeout
                    udpSocket.receive(receivePacket);

                    // Checks if there is a packet
                    if (receivePacket.getLength() > 0) {
                        // Deserialize the packet
                        ByteArrayInputStream byteStream = new ByteArrayInputStream(receiveData);
                        ObjectInputStream objectInputStream = new ObjectInputStream(byteStream);
                        RBUDPDatagramPacket rbudpPacket = (RBUDPDatagramPacket) objectInputStream.readObject();
                        int packetSequenceNumber = rbudpPacket.getSequenceNumber();
                        byte[] packetData = rbudpPacket.getData();
                        System.out.println("received key = " + packetSequenceNumber);

                        // Add to map of packets
                        receivedPacketData.put(packetSequenceNumber, packetData);

                        // Calculate the progress
                        double progress = (double) receivedPacketData.size() / totalNumberPackets;

                        // Update the progress bar in ReceiverController
                        controller.setUDPProgress(progress);

                    } else {
                        System.out.println("No packet received");
                        break;
                    }

                } catch (SocketTimeoutException e) {
                    // Timeout occurred, no packet received within the specified timeout period
                    int endOfPackets = inputStream.readInt();
                    System.out.println("signal = "+endOfPackets);


                    List<Integer> receivedSequenceNumbers = (List<Integer>) listInputStream.readObject();
                    System.out.println(receivedSequenceNumbers);

                    // Create a list of missing packets
                    List<Integer> missingSequenceNumbers = new ArrayList<>();
                    for (int seqNum : receivedSequenceNumbers) {
                        if (!receivedPacketData.containsKey(seqNum)) {
                            System.out.println("missing = " + seqNum);
                            missingSequenceNumbers.add(seqNum);
                        }
                    }

                    // Check if missing packets lists is empty or not
                    if (!missingSequenceNumbers.isEmpty()) {
                        System.out.println("there are missing packets");
                        // Tell sender to send missing files
                        DataOutputStream outputStream= new DataOutputStream(clientSocket.getOutputStream());
                        outputStream.writeBoolean(false);

                        objectOutputStream.writeObject(missingSequenceNumbers);
                        outputStream.flush(); // Ensure all data is flushed to the underlying stream

                    } else {
                        System.out.println("there are no missing packets");
                        DataOutputStream outputStream= new DataOutputStream(clientSocket.getOutputStream());
                        outputStream.writeBoolean(true);
                        //outputStream.writeObject(true);
                        //outputStream.flush();
                    }

                }

            }

            System.out.println("**************");

            int endOfPackets = inputStream.readInt();
            System.out.println("signal = "+endOfPackets);

            List<Integer> receivedSequenceNumbers = (List<Integer>) listInputStream.readObject();
            System.out.println(receivedSequenceNumbers);

            System.out.println("there are no missing packets");
            DataOutputStream outputStream= new DataOutputStream(clientSocket.getOutputStream());
            outputStream.writeBoolean(true);

            System.out.println("File complete");

            // Place the packets in correct order to build full file content in bytes
            byte[] reconstructedFileData = new byte[expectedTotalFileSize];
            int offset = 0;
            List<Integer> sortedSequenceNumbers = new ArrayList<>(receivedPacketData.keySet());
            Collections.sort(sortedSequenceNumbers); // Sort the sequence numbers

            for (int seqNum : sortedSequenceNumbers) {
                byte[] data = receivedPacketData.get(seqNum);
                System.arraycopy(data, 0, reconstructedFileData, offset, data.length);
                offset += data.length;
            }

            controller.addUDPFile(fileName);

            // Specify the destination folder
            String destinationFolder = "downloadedFiles/";
            File file = new File(destinationFolder + fileName);

            // Write the file content to the destination file
            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                fileOutputStream.write(reconstructedFileData);
                System.out.println("File downloaded and saved to: " + file.getAbsolutePath());
            } catch (Exception e) {
                System.out.println("Failed to save file: " + e.getMessage());
            }
        } catch (IOException e) {
            // Handle the case where the connection is lost
            System.out.println("Connection to Sender is lost!");
            e.printStackTrace();
            return false;
        }

        return  true;
    }

    public static boolean handleTCP(DataInputStream inputStream) {
        try {
            int fileNameLength = inputStream.readInt();

            if (fileNameLength > 0) {
                byte[] fileNameBytes = new byte[fileNameLength];
                inputStream.readFully(fileNameBytes, 0, fileNameBytes.length);
                String fileName = new String(fileNameBytes);

                int fileContentLength = inputStream.readInt();

                if(fileContentLength > 0) {

                    long totalFileSize = fileContentLength;
                    long bytesRead = 0;

                    byte[] fileContentBytes = new byte[fileContentLength];

                    while (bytesRead <= totalFileSize) {

                        // Calculate the progress
                        double progress = (double) bytesRead / totalFileSize;

                        // Update the progress bar in ReceiverController
                        controller.setTCPProgress(progress);

                        // Set the amount of bytes to read per loop
                        int bytesToRead = 1000;

                        // Read data chunk
                        int chunkSize = (int) Math.min(fileContentBytes.length - bytesRead, bytesToRead);
                        int bytes = inputStream.read(fileContentBytes, (int) bytesRead, chunkSize);

                        // Update bytesRead
                        if (bytes > 0) {
                            bytesRead += bytes;
                        } else {
                            // Break out of loop when no more bytes are being read
                            break;
                        }
                    }

                    controller.addTCPFile(fileName);

                    // Specify the destination folder
                    String destinationFolder = "downloadedFiles/";
                    File file = new File(destinationFolder + fileName);

                    // Write the file content to the destination file
                    try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                        fileOutputStream.write(fileContentBytes);
                        System.out.println("File downloaded and saved to: " + file.getAbsolutePath());
                    } catch (Exception e) {
                        System.out.println("Failed to save file: " + e.getMessage());
                    }

                }
            }

        } catch (IOException e) {
            // Handle the case where the connection is lost
            System.out.println("Connection to Sender is lost!");
            return false;
        }

        return true;
    }

}
