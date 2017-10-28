import java.io.*;
import java.net.*;

public class Server {

  private static final long TOTAL_SIZE = 1024*1024*1024; // 1GB
  private static final Integer ACK_BYTE = 123;

  public static byte[] dataBuffer = new byte[65536];
  public static byte[] applicationMessage = new byte[3];

  public enum TransportProtocol {
    TCP, UDP
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      printUsage();
    }

    int port = 0;
    TransportProtocol transportProtocol = TransportProtocol.TCP;

    try {
      port = Integer.parseInt(args[0]);
      transportProtocol = TransportProtocol.valueOf(args[1].toUpperCase());
    } catch (Exception e) {
      printUsage();
    }

    if (transportProtocol == TransportProtocol.TCP) {
      runTcp(port);
    } else {
      runUdp(port);
    }
  }

  public static void runTcp(int port) throws IOException {
    System.out.println("Running on port " + port + " over TCP");
    ServerSocket serverSocket = new ServerSocket(port);

    while (true) {
      System.out.println();
      System.out.println("Waiting for client to connect...");
      Socket clientSocket = serverSocket.accept();

      System.out.println("========== Client connected ===========");
      InputStream inputStream = clientSocket.getInputStream();
      OutputStream outputStream = clientSocket.getOutputStream();

      int count = inputStream.read(applicationMessage);
      if (count != 3) {
        System.out.println("Error: expected 3 byte message from client (only received " + count + "). Try again.");
      } else {
        System.out.println("Received " + count + " bytes from client.");
        int messageSize = (int) Math.pow(2, applicationMessage[0]);
        boolean acknowledge = applicationMessage[1] != 0;
        int checksum = applicationMessage[2];
        if (checksum != applicationMessage[0] + applicationMessage[1]) {
          System.out.println("Improper message from client: bad checksum");
        } else {
          System.out.println("Received trial configuration: { message size: " + messageSize + " bytes; protocol: " + (acknowledge ? "stop-and-wait" : "streaming") + " }");
          System.out.println("Accepting data transfer...");

          long totalBytesRead = 0;

          while (totalBytesRead < TOTAL_SIZE) {
            int dataCount = inputStream.read(dataBuffer);
            totalBytesRead += dataCount;
            if (acknowledge) {
              outputStream.write(ACK_BYTE);
            }
          }

          System.out.println("Data transfer complete: read " + totalBytesRead + " bytes from client");
        }
      }

      clientSocket.close();
      System.out.println("======== Closed connection to client ==========");
    }
  }

  public static void runUdp(int port) throws IOException {
    System.out.println("Running on port " + port + " over UDP");

    DatagramSocket socket = new DatagramSocket(port);
    DatagramPacket applicationMessagePacket = new DatagramPacket(applicationMessage, applicationMessage.length);
    DatagramPacket messagePacket = new DatagramPacket(dataBuffer, dataBuffer.length);

    while (true) {
      System.out.println("\nWaiting for client...");
      socket.receive(applicationMessagePacket);
      System.out.println("Received " + applicationMessagePacket.getLength() + " bytes from client.");
      
      if (applicationMessagePacket.getLength() != 3) {
        System.out.println(String.format(
            "Error: expected 3 byte message from client but only received %d. Try again.",
            applicationMessagePacket.getLength()));
      } else {
        int messageSize = (int) Math.pow(2, applicationMessage[0]);
        boolean acknowledge = applicationMessage[1] != 0;
        int checksum = applicationMessage[2];

        byte[] ackByte = {ACK_BYTE.byteValue()};
        DatagramPacket ackPacket = new DatagramPacket(ackByte, 0, ackByte.length, applicationMessagePacket.getAddress(), port);


        if (checksum != applicationMessage[0] + applicationMessage[1]) {
          System.out.println("Improper message from client: bad checksum");
        } else {
          System.out.println("Received trial configuration: { message size: " + messageSize + " bytes; protocol: " + (acknowledge ? "stop-and-wait" : "streaming") + " }");

          System.out.println("Accepting data transfer...");

          long totalBytesRead = 0;

          while (totalBytesRead < TOTAL_SIZE) {
            socket.receive(messagePacket);
            if (messagePacket.getLength() != messageSize) {
              System.out.println("Error: message was not agreed-upon size (received " + messagePacket.getLength() + " bytes)");
              break;
            }
            totalBytesRead += messagePacket.getLength();
            if (acknowledge) {
              socket.send(ackPacket);
            }
          }

          System.out.println("Data transfer complete: read " + totalBytesRead + " bytes from client");
        }
      }

      System.out.println("======== Closed connection to client ==========");
    }
  }

  public static void printUsage() {
    System.out.println("Usage: java Server <port> <transport protocol>");
    System.out.println("transport protocol: \"tcp\" or \"udp\"");
    System.exit(0);
  }
}
