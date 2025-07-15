package tr.unvercanunlu.ride_share.dao.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import tr.unvercanunlu.ride_share.config.AppConfig;
import tr.unvercanunlu.ride_share.core.InMemoryDao;
import tr.unvercanunlu.ride_share.dao.IRideRepository;
import tr.unvercanunlu.ride_share.entity.Ride;
import tr.unvercanunlu.ride_share.status.RideStatus;

public class RideRepository extends InMemoryDao<Ride> implements IRideRepository {

  @Override
  public List<Ride> getRequestedRidesBetweenGap(LocalDateTime gapStart, LocalDateTime gapEnd) {
    return entities.values()
        .stream()
        .filter(ride -> RideStatus.REQUESTED.equals(ride.getStatus()))
        .filter(ride -> gapStart.isAfter(ride.getRequestedAt()) && gapEnd.isBefore(ride.getRequestEndAt()))
        .toList();
  }

  @Override
  public List<Ride> getByDriver(UUID driverId) {
    return entities.values()
        .stream()
        .filter(ride -> driverId.equals(ride.getDriverId()))
        .toList();
  }

  @Override
  public List<Ride> getByPassenger(UUID passengerId) {
    return entities.values()
        .stream()
        .filter(ride -> passengerId.equals(ride.getPassengerId()))
        .toList();
  }

  @Override
  public boolean checkActiveRideForPassenger(UUID passengerId) {
    return getByPassenger(passengerId)
        .stream()
        .anyMatch(ride -> AppConfig.ACTIVE_RIDE_STATUSES.contains(ride.getStatus()));
  }

  @Override
  public boolean checkActiveRideForDriver(UUID driverId) {
    return getByDriver(driverId)
        .stream()
        .anyMatch(ride -> AppConfig.ACTIVE_RIDE_STATUSES.contains(ride.getStatus()));
  }

}
