package tr.unvercanunlu.ride_share.exception;

public abstract class RideSharingBaseException extends RuntimeException {

  protected RideSharingBaseException(String message, Object... parameters) {
    super(message.formatted(parameters));
  }

}
