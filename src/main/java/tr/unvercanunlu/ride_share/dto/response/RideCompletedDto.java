package tr.unvercanunlu.ride_share.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import tr.unvercanunlu.ride_share.entity.Location;

public record RideCompletedDto(
    UUID id,
    UUID passengerId,
    UUID driverId,
    Location pickup,
    Location dropOff,
    double distance,
    BigDecimal fare,
    LocalDateTime requestedAt,
    LocalDateTime acceptedAt,
    LocalDateTime pickupAt,
    LocalDateTime completedAt,
    int duration
) {

}
