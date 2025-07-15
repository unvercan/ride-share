package tr.unvercanunlu.ride_share.exception;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import tr.unvercanunlu.ride_share.config.ErrorMessage;

public class PassengerNotFoundException extends RuntimeException {

  @Getter
  private final UUID id;

  public PassengerNotFoundException(UUID id) {
    super(ErrorMessage.PASSENGER_NOT_FOUND.formatted(Objects.toString(id, null)));

    this.id = id;
  }

}
