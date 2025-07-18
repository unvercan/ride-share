package tr.unvercanunlu.ride_share.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import tr.unvercanunlu.ride_share.core.BaseEntity;
import tr.unvercanunlu.ride_share.dto.request.RequestRideDto;
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
  private long duration;
  private BigDecimal fare;
  private LocalDateTime requestedAt;
  private LocalDateTime requestEndAt;
  private LocalDateTime acceptedAt;
  private LocalDateTime approvedAt;
  private LocalDateTime pickupAt;
  private LocalDateTime pickupEndAt;
  private LocalDateTime completedAt;
  private LocalDateTime canceledAt;

  public static Ride of(RequestRideDto request) {
    Ride ride = new Ride();

    ride.setPassengerId(request.passengerId());
    ride.setPickup(request.pickup());
    ride.setDropOff(request.dropOff());
    ride.setStatus(RideStatus.REQUESTED);

    return ride;
  }

}
