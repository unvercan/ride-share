package tr.unvercanunlu.ride_share.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import tr.unvercanunlu.ride_share.entity.Location;

public record RideCanceledDto(
    UUID id,
    UUID passengerId,
    UUID driverId,
    Location pickup,
    Location dropOff,
    double distance,
    double fare,
    LocalDateTime requestedAt,
    LocalDateTime canceledAt
) {

}
