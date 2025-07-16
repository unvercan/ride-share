package tr.unvercanunlu.ride_share.exception;

import java.util.UUID;
import lombok.Getter;
import tr.unvercanunlu.ride_share.config.ErrorMessage;

public class PassengerHasActiveRideException extends RideSharingException {

  @Getter
  private final UUID id;

  public PassengerHasActiveRideException(UUID passengerId) {
    super(ErrorMessage.PASSENGER_HAS_ACTIVE_RIDE, passengerId);

    this.id = passengerId;
  }

}
