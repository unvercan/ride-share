package tr.unvercanunlu.ride_share.exception;

import java.util.UUID;
import lombok.Getter;
import tr.unvercanunlu.ride_share.config.ErrorMessage;

public class RideAlreadyCompletedException extends RideSharingBaseException {

  @Getter
  private final UUID rideId;

  public RideAlreadyCompletedException(UUID rideId) {
    super(ErrorMessage.RIDE_ALREADY_COMPLETED, rideId);
    this.rideId = rideId;
  }

}
