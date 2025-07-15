package tr.unvercanunlu.ride_share.exception;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import tr.unvercanunlu.ride_share.config.ErrorMessage;

public class DriverHasActiveRideException extends RuntimeException {

  @Getter
  private final UUID id;

  public DriverHasActiveRideException(UUID driverId) {
    super(ErrorMessage.DRIVER_HAS_ACTIVE_RIDE.formatted(Objects.toString(driverId, null)));

    this.id = driverId;
  }

}
