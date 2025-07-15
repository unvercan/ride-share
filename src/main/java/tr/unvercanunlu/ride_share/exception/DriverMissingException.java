package tr.unvercanunlu.ride_share.exception;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import tr.unvercanunlu.ride_share.config.ErrorMessage;

public class DriverMissingException extends RuntimeException {

  @Getter
  private final UUID id;

  public DriverMissingException(UUID rideId) {
    super(ErrorMessage.DRIVER_MISSING.formatted(Objects.toString(rideId, null)));

    this.id = rideId;
  }

}
