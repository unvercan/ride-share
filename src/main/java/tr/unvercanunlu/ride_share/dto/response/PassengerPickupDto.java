package tr.unvercanunlu.ride_share.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import tr.unvercanunlu.ride_share.entity.Location;

public record PassengerPickupDto(
    UUID id,
    UUID passengerId,
    UUID driverId,
    Location pickup,
    Location dropOff,
    double distance,
    double fare,
    LocalDateTime requestedAt,
    LocalDateTime acceptedAt,
    LocalDateTime pickupAt,

    // estimations
    int estimatedDuration,
    LocalDateTime estimatedCompletedAt
) {

}
