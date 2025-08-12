package tr.unvercanunlu.ride_share.dto.response;

import java.time.LocalDateTime;

public record EstimationDto(
    Integer duration,
    LocalDateTime pickupAt,
    LocalDateTime pickupEndAt,
    LocalDateTime completedAt
) {

}
