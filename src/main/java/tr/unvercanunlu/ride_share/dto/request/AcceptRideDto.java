package tr.unvercanunlu.ride_share.dto.request;

import java.util.UUID;
import tr.unvercanunlu.ride_share.entity.Location;

public record AcceptRideDto(
    UUID rideId,
    UUID driverId,
    Location current
) {

}
