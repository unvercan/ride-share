package tr.unvercanunlu.ride_share.exception;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import tr.unvercanunlu.ride_share.config.ErrorMessage;

public class DriverUnavailableException extends RuntimeException {

  @Getter
  private final UUID id;

  public DriverUnavailableException(UUID driverId) {
    super(ErrorMessage.DRIVER_UNAVAILABLE.formatted(Objects.toString(driverId, null)));

    this.id = driverId;
  }

}
