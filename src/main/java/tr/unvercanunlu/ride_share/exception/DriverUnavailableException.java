package tr.unvercanunlu.ride_share.exception;

import java.util.UUID;
import lombok.Getter;
import tr.unvercanunlu.ride_share.config.ErrorMessage;

public class DriverUnavailableException extends RideSharingException {

  @Getter
  private final UUID id;

  public DriverUnavailableException(UUID driverId) {
    super(ErrorMessage.DRIVER_UNAVAILABLE, driverId);

    this.id = driverId;
  }

}
