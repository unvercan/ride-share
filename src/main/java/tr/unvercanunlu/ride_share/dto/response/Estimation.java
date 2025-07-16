package tr.unvercanunlu.ride_share.dto.response;

import java.time.LocalDateTime;

public record Estimation(
    Integer duration,
    LocalDateTime pickupAt,
    LocalDateTime pickupEndAt,
    LocalDateTime completedAt
) {

}
