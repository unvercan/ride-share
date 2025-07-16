package tr.unvercanunlu.ride_share.exception;

import java.util.UUID;
import lombok.Getter;
import tr.unvercanunlu.ride_share.config.ErrorMessage;

public class DriverMissingForRideException extends RideSharingException {

  @Getter
  private final UUID id;

  public DriverMissingForRideException(UUID rideId) {
    super(ErrorMessage.DRIVER_MISSING_FOR_RIDE, rideId);

    this.id = rideId;
  }

}
