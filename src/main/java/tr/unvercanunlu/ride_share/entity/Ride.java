package tr.unvercanunlu.ride_share.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import tr.unvercanunlu.ride_share.core.BaseEntity;
import tr.unvercanunlu.ride_share.status.RideStatus;

@Getter
@Setter
public class Ride extends BaseEntity<UUID> {

  private UUID driverId;
  private UUID passengerId;
  private Location pickup;
  private Location dropOff;
  private RideStatus status;
  private double distance;
  private int duration;
  private double fare;
  private LocalDateTime requestedAt;
  private LocalDateTime requestEndAt;
  private LocalDateTime acceptedAt;
  private LocalDateTime pickupAt;
  private LocalDateTime pickupEndAt;
  private LocalDateTime completedAt;
  private LocalDateTime canceledAt;

}
