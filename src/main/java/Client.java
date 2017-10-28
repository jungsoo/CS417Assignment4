import java.io.IOException;
import java.net.*;

public class Client {

  private static final long TOTAL_SIZE = 10;
  private static final Integer ACK_BYTE = 255;

  public enum TransportProtocol {
    TCP, UDP
  }

  public enum AckProtocol {
    STREAMING, STOPANDWAIT
  }

  public static void main(String[] args) {
    if (args.length != 5) {
      printUsage();
    }

    try {
      InetAddress address = InetAddress.getByName(args[0]);
      int port = Integer.parseInt(args[1]);
      TransportProtocol transportProtocol = TransportProtocol.valueOf(args[2]);
      AckProtocol ackProtocol = AckProtocol.valueOf(args[3]);
      int messageSize = Integer.parseInt(args[4]);

      if (messageSize < 0 || messageSize > 16) {
        printUsage();
      }

      if (transportProtocol == TransportProtocol.TCP) {
        runTcp(address, port, ackProtocol, messageSize);
      } else {
        runUdp(address, port, ackProtocol, messageSize);
      }

    } catch (Exception e) {
      printUsage();
    }
  }

  private static void runUdp(InetAddress address,
                             int port,
                             AckProtocol ackProtocol,
                             Integer messageSize) throws IOException {

    DatagramSocket socket = new DatagramSocket(port);
    byte[] applicationMessage = new byte[3];
    applicationMessage[0] = messageSize.byteValue();
    applicationMessage[1] = Integer.valueOf(ackProtocol ==  AckProtocol.STOPANDWAIT ? 1 : 0).byteValue();
    applicationMessage[2] = Integer.valueOf(applicationMessage[0] + applicationMessage[1]).byteValue();

    DatagramPacket applicationMessagePacket = new DatagramPacket(
        applicationMessage,
        0,
        applicationMessage.length,
        address,
        port);
    DatagramPacket messagePacket = new DatagramPacket(
        new byte[messageSize],
        0,
        messageSize,
        address,
        port
    );
    DatagramPacket ackPacket = new DatagramPacket(
        new byte[1],
        1
    );

    socket.send(applicationMessagePacket);

    long start = System.currentTimeMillis();
    long count = TOTAL_SIZE;
    while (count > 0) {
      socket.send(messagePacket);
      count -= messageSize;
      if (ackProtocol == AckProtocol.STOPANDWAIT) {
        socket.receive(ackPacket);
        if (ackPacket.getData()[0] == 255) {
          continue;
        } else {
          System.err.println("Received response, but not ack byte");
          System.exit(1);
        }
      }
    }
    System.out.println(System.currentTimeMillis() - start);
  }

  private static void runTcp(InetAddress address, int port, AckProtocol ackProtocol, int messageSize) {
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
  }
}
