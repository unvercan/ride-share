package tr.unvercanunlu.ride_share.dao.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import tr.unvercanunlu.ride_share.config.AppConfig;
import tr.unvercanunlu.ride_share.core.impl.InMemoryDaoImpl;
import tr.unvercanunlu.ride_share.dao.RideRepository;
import tr.unvercanunlu.ride_share.entity.Ride;
import tr.unvercanunlu.ride_share.status.RideStatus;

public class RideRepositoryImpl extends InMemoryDaoImpl<Ride> implements RideRepository {

  @Override
  public List<Ride> getRequestedRidesBetweenGap(LocalDateTime gapStart, LocalDateTime gapEnd) {
    if ((gapStart == null) || (gapEnd == null)) {
      return List.of();
    }

    return entities.values()
        .stream()
        .filter(ride -> ride.getStatus() != null)
        .filter(ride -> ride.getRequestedAt() != null)
        .filter(ride -> RideStatus.REQUESTED.equals(ride.getStatus()))
        .filter(ride -> !ride.getRequestedAt().isBefore(gapStart) && !ride.getRequestedAt().isAfter(gapEnd)).toList();
  }

  @Override
  public List<Ride> getByDriver(UUID driverId) {
    if (driverId == null) {
      return List.of();
    }

    return entities.values()
        .stream()
        .filter(ride -> ride.getDriverId() != null)
        .filter(ride -> driverId.equals(ride.getDriverId()))
        .toList();
  }

  @Override
  public List<Ride> getByPassenger(UUID passengerId) {
    if (passengerId == null) {
      return List.of();
    }

    return entities.values()
        .stream()
        .filter(ride -> ride.getPassengerId() != null)
        .filter(ride -> passengerId.equals(ride.getPassengerId()))
        .toList();
  }

  @Override
  public boolean checkActiveRideExistsForPassenger(UUID passengerId) {
    return (passengerId != null)
        && getByPassenger(passengerId)
        .stream()
        .filter(ride -> ride.getStatus() != null)
        .anyMatch(ride -> AppConfig.ACTIVE_RIDE_STATUSES.contains(ride.getStatus()));
  }

  @Override
  public boolean checkActiveRideExistsForDriver(UUID driverId) {
    return (driverId != null)
        && getByDriver(driverId)
        .stream()
        .filter(ride -> ride.getStatus() != null)
        .anyMatch(ride -> AppConfig.ACTIVE_RIDE_STATUSES.contains(ride.getStatus()));
  }

}
