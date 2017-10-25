public enum TransportProtocol {

  TCP("tcp"),
  UDP("udp");

  private String protocol;

  TransportProtocol(String protocol) {
    this.protocol = protocol;
  }

  public String getProtocol() {
    return protocol;
  }
}
