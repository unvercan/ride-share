package tr.unvercanunlu.ride_share.exception;

public class RideSharingException extends RuntimeException {

  public RideSharingException(String message, Object... parameters) {
    super(message.formatted(parameters));
  }

}
