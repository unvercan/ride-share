package tr.unvercanunlu.ride_share.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import tr.unvercanunlu.ride_share.entity.Location;

@Builder
@Data
public class NearRequestedRideDto {

  private UUID id;
  private UUID passengerId;
  private Location currentLocation;
  private Location pickUpLocation;
  private Location dropOffLocation;
  private LocalDateTime requestedAt;
  private LocalDateTime requestEndAt;
  private LocalDateTime estimatedPickedUpStartAt;
  private LocalDateTime estimatedPickedUpEndAt;
  private LocalDateTime estimatedCompletedAt;
  private double distanceToPickupLocation;
  private double fare;

}
