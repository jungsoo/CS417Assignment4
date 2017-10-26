import java.util.Scanner;

public class Server {
  public enum Protocol { TCP, UDP };

  public static void main(String[] args) {
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

    System.out.println("Running on port " + port + " on protocol " + protocol.name());
  }

  public static void print_usage() {
    System.out.println("Usage: server <port> <transport protocol>");
    System.out.println("transport protocol: \"tcp\" or \"udp\"");
    System.exit(0);
  }
}
