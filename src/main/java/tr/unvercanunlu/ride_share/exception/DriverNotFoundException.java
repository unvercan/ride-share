package tr.unvercanunlu.ride_share.exception;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import tr.unvercanunlu.ride_share.config.ErrorMessage;

public class DriverNotFoundException extends RuntimeException {

  @Getter
  private final UUID id;

  public DriverNotFoundException(UUID id) {
    super(ErrorMessage.DRIVER_NOT_FOUND.formatted(Objects.toString(id, null)));

    this.id = id;
  }

}
