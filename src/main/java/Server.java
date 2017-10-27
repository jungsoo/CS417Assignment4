import java.io.*;
import java.net.*;

public class Server {
  public enum Protocol { TCP, UDP };
  public static final long TOTAL_SIZE = 10;
  public static final int ACK_BYTE = 255;
  public static byte[] dataBuffer = new byte[65536];
  public static byte[] applicationMessage = new byte[3];

  public static void main(String[] args) throws java.io.IOException {
    if (args.length != 2) print_usage();

    int port = 0;
    Protocol protocol = Protocol.TCP;

    try
    {
      port = Integer.parseInt(args[0]);
      protocol = Protocol.valueOf(args[1].toUpperCase());
    }
    catch (Exception e)
    {
      print_usage();
    }

    if (protocol == Protocol.TCP) {
      run_tcp(port);
    } else {
      run_udp(port);
    }
  }

  public static void run_tcp(int port) throws java.io.IOException {
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
        int messageSize = (int)Math.pow(2, applicationMessage[0]);
        boolean acknowledge = applicationMessage[1] != 0;
        int checksum = applicationMessage[2];
        if (checksum != applicationMessage[0] + applicationMessage[1]) {
          System.out.println("Improper message from client: bad checksum");
        } else {
          System.out.println("Client has requested:");
          System.out.println("Message size: " + messageSize + " bytes");
          System.out.println("Acknowledgement protocol: " + (acknowledge ? "stop-and-wait" : "streaming"));

          System.out.println("Accepting data transfer...");

          long totalBytesRead = 0;

          while (totalBytesRead < TOTAL_SIZE) {
            int dataCount = inputStream.read(dataBuffer);
            if (dataCount != messageSize) {
              System.out.println("Error: message was not agreed-upon size (received " + dataCount + " bytes)");
              break;
            }
            System.out.println("Received " + dataCount + " bytes"); // todo: remove
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

  public static void run_udp(int port) throws java.io.IOException {
    System.out.println("Running on port " + port + " over UDP");
  }

  public static void print_usage() {
    System.out.println("Usage: server <port> <transport protocol>");
    System.out.println("transport protocol: \"tcp\" or \"udp\"");
    System.exit(0);
  }
}
