package ks.relay.common.protocol.exception;

public class EndOfDataException extends ReadProtocolMsgException {
  private static final long serialVersionUID = 1L;
  
  public EndOfDataException() {
    super();
  }
  
  public EndOfDataException(String message) {
    super(message);
  }

  public EndOfDataException(String message, Throwable cause) {
    super(message, cause);
  }

  public EndOfDataException(Throwable cause) {
    super(cause);
  }

}
