import java.util.Scanner;

public class Client {

  public static void main(String[] args) {
    Scanner input = new Scanner(System.in);
    System.out.print("Hostname: ");
    String hostname = input.nextLine();
    System.out.print("Port: ");
    String port = input.nextLine();
    String transport = "";
    while (!transport.equals("1") && !transport.equals("2")) {
      System.out.print("Transport Protocol [(1) UDP (2) TCP]: ");
      transport = input.nextLine();
    }
    String acknowledgement = "";
    while (!acknowledgement.equals("1") && !acknowledgement.equals("2")) {
      System.out.print("Acknowledgement Protocol [(1) Streaming, (2) Stop-and-wait]: ");
      acknowledgement = input.nextLine();
    }
    System.out.print("Message size [2^n, 0 <= n <= 16]: ");
    String messageSize = input.nextLine();

    System.out.println(String.format("%s:%s -- %s -- %s -- %s", hostname, port, transport, acknowledgement, messageSize));
  }
}
