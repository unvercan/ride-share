package tr.unvercanunlu.ride_share.exception;

import java.util.UUID;
import lombok.Getter;
import tr.unvercanunlu.ride_share.config.ErrorMessage;

public class PassengerNotFoundException extends RideSharingException {

  @Getter
  private final UUID id;

  public PassengerNotFoundException(UUID passengerId) {
    super(ErrorMessage.PASSENGER_NOT_FOUND, passengerId);

    this.id = passengerId;
  }

}
