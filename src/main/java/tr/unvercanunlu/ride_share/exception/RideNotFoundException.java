package tr.unvercanunlu.ride_share.exception;

import java.util.UUID;
import lombok.Getter;
import tr.unvercanunlu.ride_share.config.ErrorMessage;

public class RideNotFoundException extends RideSharingException {

  @Getter
  private final UUID id;

  public RideNotFoundException(UUID rideId) {
    super(ErrorMessage.RIDE_NOT_FOUND, rideId);

    this.id = rideId;
  }

}
