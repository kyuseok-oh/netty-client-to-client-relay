package ks.relay.common.protocol.exception;

public class ReadProtocolMsgException extends Exception {
  private static final long serialVersionUID = 1L;

  public ReadProtocolMsgException() {
    super();
  }
  
  public ReadProtocolMsgException(String message) {
    super(message);
  }

  public ReadProtocolMsgException(String message, Throwable cause) {
    super(message, cause);
  }

  public ReadProtocolMsgException(Throwable cause) {
    super(cause);
  }
}
