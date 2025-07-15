package tr.unvercanunlu.ride_share.dto.request;

import java.util.UUID;
import tr.unvercanunlu.ride_share.entity.Location;

public record RequestRideDto(
    UUID passengerId,
    Location pickup,
    Location dropOff
) {

}
