import java.util.Scanner;

public class Client {

  public static void main(String[] args) {
    Scanner input = new Scanner(System.in);
    System.out.print("Hostname: ");
    String hostname = input.nextLine();
    System.out.print("Port: ");
    String port = input.nextLine();
    String protocol = "";
    while (!protocol.equalsIgnoreCase("udp") && !protocol.equalsIgnoreCase("tcp")) {
      System.out.print("Transport Protocol (TCP/UDP): ");
      protocol = input.nextLine();
    }
  }
}
