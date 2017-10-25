public enum AcknowledgementProtocol {

  STREAMING("streaming"),
  STOPANDWAIT("stopandwait");

  private String protocol;

  AcknowledgementProtocol(String protocol) {
    this.protocol = protocol;
  }

  public String getProtocol() {
    return protocol;
  }
}
