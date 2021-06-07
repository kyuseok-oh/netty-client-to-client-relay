package ks.relay.common.protocol.exception;

public class NoMoreDataException extends ReadProtocolMsgException {

  private static final long serialVersionUID = 1L;
  
  public NoMoreDataException() {
    super();
  }
  
  public NoMoreDataException(String message) {
    super(message);
  }

  public NoMoreDataException(String message, Throwable cause) {
    super(message, cause);
  }

  public NoMoreDataException(Throwable cause) {
    super(cause);
  }

}
