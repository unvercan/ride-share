package tr.unvercanunlu.ride_share.dao.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import tr.unvercanunlu.ride_share.config.AppConfig;
import tr.unvercanunlu.ride_share.core.impl.InMemoryDaoImpl;
import tr.unvercanunlu.ride_share.dao.RideRepository;
import tr.unvercanunlu.ride_share.entity.Ride;
import tr.unvercanunlu.ride_share.helper.LogHelper;
import tr.unvercanunlu.ride_share.status.RideStatus;

public class RideRepositoryImpl extends InMemoryDaoImpl<Ride> implements RideRepository {

  @Override
  public List<Ride> getRequestedRidesBetweenGap(LocalDateTime gapStart, LocalDateTime gapEnd) {
    LogHelper.debug(this.getClass(),
        String.format("Getting requested rides between %s and %s", gapStart, gapEnd));

    if ((gapStart == null) || (gapEnd == null)) {
      LogHelper.error(this.getClass(), "Gap start or end is null!");

      return List.of();
    }

    List<Ride> rides = entities.values()
        .stream()
        .filter(ride -> ride.getStatus() != null)
        .filter(ride -> ride.getRequestedAt() != null)
        .filter(ride -> RideStatus.REQUESTED.equals(ride.getStatus()))
        .filter(ride -> !ride.getRequestedAt().isBefore(gapStart) && !ride.getRequestedAt().isAfter(gapEnd))
        .toList();

    LogHelper.debug(this.getClass(),
        String.format("Found %d requested rides in gap", rides.size()));

    return rides;
  }

  @Override
  public List<Ride> getByDriver(UUID driverId) {
    LogHelper.debug(this.getClass(),
        String.format("Getting rides by driverId=%s", driverId));

    if (driverId == null) {
      LogHelper.error(this.getClass(), "driverId is null!");

      return List.of();
    }

    List<Ride> rides = entities.values()
        .stream()
        .filter(ride -> ride.getDriverId() != null)
        .filter(ride -> driverId.equals(ride.getDriverId()))
        .toList();

    LogHelper.debug(this.getClass(),
        String.format("Found %d rides for driverId=%s", rides.size(), driverId));

    return rides;
  }

  @Override
  public List<Ride> getByPassenger(UUID passengerId) {
    LogHelper.debug(this.getClass(),
        String.format("Getting rides by passengerId=%s", passengerId));

    if (passengerId == null) {
      LogHelper.error(this.getClass(), "passengerId is null!");

      return List.of();
    }

    List<Ride> rides = entities.values()
        .stream()
        .filter(ride -> ride.getPassengerId() != null)
        .filter(ride -> passengerId.equals(ride.getPassengerId()))
        .toList();

    LogHelper.debug(this.getClass(),
        String.format("Found %d rides for passengerId=%s", rides.size(), passengerId));

    return rides;
  }

  @Override
  public boolean checkActiveRideExistsForPassenger(UUID passengerId) {
    boolean exists = (passengerId != null)
        && getByPassenger(passengerId)
        .stream()
        .filter(ride -> ride.getStatus() != null)
        .anyMatch(ride -> AppConfig.ACTIVE_RIDE_STATUSES.contains(ride.getStatus()));

    LogHelper.debug(this.getClass(),
        String.format("Active ride exists for passengerId=%s: %b", passengerId, exists));

    return exists;
  }

  @Override
  public boolean checkActiveRideExistsForDriver(UUID driverId) {
    boolean exists = (driverId != null)
        && getByDriver(driverId)
        .stream()
        .filter(ride -> ride.getStatus() != null)
        .anyMatch(ride -> AppConfig.ACTIVE_RIDE_STATUSES.contains(ride.getStatus()));

    LogHelper.debug(this.getClass(),
        String.format("Active ride exists for driverId=%s: %b", driverId, exists));

    return exists;
  }

}
