package tr.unvercanunlu.ride_share.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import tr.unvercanunlu.ride_share.entity.Location;

public record RideRequestedDto(
    UUID id,
    UUID passengerId,
    Location pickup,
    Location dropOff,
    double distance,
    double fare,
    LocalDateTime requestedAt,
    LocalDateTime requestEndAt,

    // estimations
    int estimatedDuration
) {


}
