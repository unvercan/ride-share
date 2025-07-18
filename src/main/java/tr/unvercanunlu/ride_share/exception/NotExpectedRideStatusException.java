package tr.unvercanunlu.ride_share.exception;

import java.util.UUID;
import lombok.Getter;
import tr.unvercanunlu.ride_share.config.ErrorMessage;
import tr.unvercanunlu.ride_share.status.RideStatus;

public class NotExpectedRideStatusException extends RideSharingBaseException {

  @Getter
  private final UUID rideId;

  @Getter
  private final Object expected;

  @Getter
  private final RideStatus actual;

  public NotExpectedRideStatusException(UUID rideId, Object expected, RideStatus actual) {
    super(ErrorMessage.RIDE_STATUS_NOT_EXPECTED, expected, actual);

    this.rideId = rideId;
    this.expected = expected;
    this.actual = actual;
  }

}
