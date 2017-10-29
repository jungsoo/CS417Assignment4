import java.io.*;
import java.net.*;

public class Client {

  private static final long TOTAL_SIZE = 1024*1024*1024; // 1GB
  private static final Integer ACK_BYTE = 123;

  public enum TransportProtocol {
    TCP, UDP
  }

  public enum AckProtocol {
    STREAMING, STOPANDWAIT
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 5) {
      printUsage();
    }

    InetAddress address = null;
    int port = 0;
    TransportProtocol transportProtocol = null;
    AckProtocol ackProtocol = null;
    int messageSize = 0;

    try {
      address = InetAddress.getByName(args[0]);
      port = Integer.parseInt(args[1]);
      transportProtocol = TransportProtocol.valueOf(args[2].toUpperCase());
      ackProtocol = AckProtocol.valueOf(args[3].toUpperCase());
      messageSize = Integer.parseInt(args[4]);
    } catch (Exception e) {
      printUsage();
    }

    if (messageSize < 0 || messageSize > 16) {
        printUsage();
    }

    if (transportProtocol == TransportProtocol.TCP) {
        runTcp(address, port, ackProtocol, messageSize);
      } else {
        runUdp(address, port, ackProtocol, messageSize);
      }
  }

  private static void runUdp(InetAddress address,
                             int port,
                             AckProtocol ackProtocol,
                             Integer messageSize) throws IOException {

    System.out.print("Connecting to server " + address + " port " + port + " over UDP... ");
    DatagramSocket socket = new DatagramSocket(port);
    System.out.println("Done.");

    int outputMessageSize = (int) Math.pow(2, messageSize);

    System.out.println("Trial configuration: { message size: " + outputMessageSize + " bytes; protocol: " + ackProtocol + " }");
    
    System.out.print("Sending configuration to server... ");
    byte[] applicationMessage = createApplicationMessage(messageSize, ackProtocol);
    DatagramPacket applicationMessagePacket = new DatagramPacket(
        applicationMessage,
        0,
        applicationMessage.length,
        address,
        port);
    socket.send(applicationMessagePacket);
    System.out.println("Done.");

    DatagramPacket messagePacket = new DatagramPacket(
        new byte[outputMessageSize],
        0,
        outputMessageSize,
        address,
        port
    );
    DatagramPacket ackPacket = new DatagramPacket(
        new byte[1],
        1
    );

    System.out.print("Starting data transfer to server... ");

    long start = System.currentTimeMillis();
    long count = 0;
    long totalMessagesSent = 0;
    while (count < TOTAL_SIZE) {
      socket.send(messagePacket);
      count -= outputMessageSize;
      totalMessagesSent++;
      if (ackProtocol == AckProtocol.STOPANDWAIT) {
        socket.receive(ackPacket);
        if (ackPacket.getData()[0] == ACK_BYTE) {
          continue;
        } else {
          System.err.println("Error: expected ack from server, received something else.");
          System.exit(1);
        }
      }
    }

    long end = System.currentTimeMillis();
    System.out.println("Done.");
    System.out.println("Sent " + count + " bytes in " + totalMessagesSent + " messages (took " + (end-start) + " ms).");
  }

  private static void runTcp(InetAddress address, int port, AckProtocol ackProtocol, int messageSize)  throws IOException {
    System.out.print("Connecting to server " + address + " port " + port + " over TCP... ");
    Socket socket = new Socket(address, port);
    System.out.println("Connected.");

    InputStream inputStream = socket.getInputStream();
    OutputStream outputStream = socket.getOutputStream();

    int outputMessageSize = (int) Math.pow(2, messageSize);

    System.out.println("Trial configuration: { message size: " + outputMessageSize + " bytes; protocol: " + ackProtocol + " }");
    System.out.print("Sending configuration to server... ");
    byte[] applicationMessage = createApplicationMessage(messageSize, ackProtocol);
    outputStream.write(applicationMessage);
    System.out.println("Done.");

    byte[] inputBuffer = new byte[10];
    byte[] outputBuffer = new byte[outputMessageSize];
    long totalBytesSent = 0;

    System.out.print("Starting data transfer to server... ");
    long start = System.currentTimeMillis();
    long totalMessagesSent = 0;

    while (totalBytesSent < TOTAL_SIZE) {
      outputStream.write(outputBuffer);
      totalBytesSent += outputMessageSize;
      totalMessagesSent++;
      if (ackProtocol == AckProtocol.STOPANDWAIT) {
        int bytesReceived = inputStream.read(inputBuffer);
        if (inputBuffer[0] != ACK_BYTE) {
          System.err.println("Error: expected ack from server, received something else.");
          System.exit(1);
        }
      }
    }

    long end = System.currentTimeMillis();
    System.out.println("Done.");
    System.out.println("Sent " + totalBytesSent + " bytes in " + totalMessagesSent + " messages (took " + (end-start) + " ms).");
    socket.close();
    System.out.println("Connection closed.");
  }

  private static byte[] createApplicationMessage(int messageSize, AckProtocol ackProtocol) {
    byte[] ret = new byte[3];
    ret[0] = (byte)messageSize;
    ret[1] = (ackProtocol == AckProtocol.STOPANDWAIT) ? (byte)1 : (byte)0;
    ret[2] = (byte)(ret[0] + ret[1]);
    return ret;
  }

  private static void printUsage() {
    System.out.println("Usage: java Client " +
        "<hostname> " +
        "<port> " +
        "<transport protocol> " +
        "<ack protocol> " +
        "<msg size>");
    System.out.println("transport protocol: \"tcp\" or \"udp\"");
    System.out.println("acknowledgement protocol: \"streaming\" or \"stopandwait\"");
    System.out.println("message size: 0 <= n <= 16 (2^n)");
    System.exit(0);
  }
}
