package tr.unvercanunlu.ride_share.exception;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import tr.unvercanunlu.ride_share.config.ErrorMessage;

public class PassengerHasActiveRideException extends RuntimeException {

  @Getter
  private final UUID id;

  public PassengerHasActiveRideException(UUID passengerId) {
    super(ErrorMessage.PASSENGER_HAS_ACTIVE_RIDE.formatted(Objects.toString(passengerId, null)));

    this.id = passengerId;
  }

}
