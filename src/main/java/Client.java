import java.net.InetAddress;
import java.net.UnknownHostException;

public class Client {

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

  private static void runUdp(InetAddress address, int port, AckProtocol ackProtocol, int messageSize) {
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
