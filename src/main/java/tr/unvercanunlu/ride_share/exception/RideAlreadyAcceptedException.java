package tr.unvercanunlu.ride_share.exception;

import java.util.UUID;
import lombok.Getter;
import tr.unvercanunlu.ride_share.config.ErrorMessage;

public class RideAlreadyAcceptedException extends RideSharingBaseException {

  @Getter
  private final UUID rideId;

  public RideAlreadyAcceptedException(UUID rideId) {
    super(ErrorMessage.RIDE_ALREADY_ACCEPTED, rideId);
    this.rideId = rideId;
  }

}
