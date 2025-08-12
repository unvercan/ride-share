package tr.unvercanunlu.ride_share.exception;

import java.util.UUID;
import lombok.Getter;
import tr.unvercanunlu.ride_share.config.ErrorMessage;

public class DriverUnavailableException extends RideSharingBaseException {

  @Getter
  private final UUID driverId;

  public DriverUnavailableException(UUID driverId) {
    super(ErrorMessage.DRIVER_UNAVAILABLE, driverId);
    this.driverId = driverId;
  }

}
