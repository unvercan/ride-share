package tr.unvercanunlu.ride_share.exception;

import java.util.UUID;
import lombok.Getter;
import tr.unvercanunlu.ride_share.config.ErrorMessage;

public class DriverNotFoundException extends RideSharingException {

  @Getter
  private final UUID id;

  public DriverNotFoundException(UUID driverId) {
    super(ErrorMessage.DRIVER_NOT_FOUND, driverId);

    this.id = driverId;
  }

}
