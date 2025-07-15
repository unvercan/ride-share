package tr.unvercanunlu.ride_share.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import tr.unvercanunlu.ride_share.entity.Location;

public record NearRequestedRideDto(
    UUID id,
    UUID passengerId,
    Location pickup,
    Location dropOff,
    double distance,
    BigDecimal fare,
    LocalDateTime requestedAt,
    LocalDateTime requestEndAt,
    Location current,
    double distanceToPickup,

    // estimations
    LocalDateTime estimatedPickupAt,
    LocalDateTime estimatedPickupEndAt,
    LocalDateTime estimatedCompletedAt
) {

}
