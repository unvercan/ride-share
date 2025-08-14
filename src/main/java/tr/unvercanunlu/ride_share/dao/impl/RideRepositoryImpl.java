package tr.unvercanunlu.ride_share.dao.impl;

import static tr.unvercanunlu.ride_share.config.AppConfig.ACTIVE_RIDE_STATES;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import tr.unvercanunlu.ride_share.core.dao.impl.InMemoryDaoImpl;
import tr.unvercanunlu.ride_share.dao.RideRepository;
import tr.unvercanunlu.ride_share.entity.Ride;
import tr.unvercanunlu.ride_share.status.RideStatus;

@Slf4j
public class RideRepositoryImpl extends InMemoryDaoImpl<Ride> implements RideRepository {

  @Override
  public List<Ride> findRequestedWithinWindow(LocalDateTime windowStart, LocalDateTime windowEnd) {
    log.debug("Getting requested rides between {} and {}", windowStart, windowEnd);
    if ((windowStart == null) || (windowEnd == null)) {
      log.error("Gap start or end is null!");
      return List.of();
    }

    List<Ride> rides = entities.values()
        .stream()
        .filter(ride -> ride.getStatus() != null)
        .filter(ride -> ride.getRequestedAt() != null)
        .filter(ride -> RideStatus.REQUESTED.equals(ride.getStatus()))
        .filter(ride -> !ride.getRequestedAt().isBefore(windowStart) && !ride.getRequestedAt().isAfter(windowEnd))
        .toList();

    log.debug("Found {} requested rides in gap", rides.size());
    return rides;
  }

  @Override
  public List<Ride> findAllByDriverId(UUID driverId) {
    if (driverId == null) {
      throw new IllegalArgumentException("ID missing!");
    }

    List<Ride> rides = entities.values()
        .stream()
        .filter(ride -> ride.getDriverId() != null)
        .filter(ride -> driverId.equals(ride.getDriverId()))
        .toList();

    log.debug("Found {} rides for driverId={}", rides.size(), driverId);
    return rides;
  }

  @Override
  public List<Ride> findAllByPassengerId(UUID passengerId) {
    if (passengerId == null) {
      throw new IllegalArgumentException("ID missing!");
    }

    List<Ride> rides = entities.values()
        .stream()
        .filter(ride -> ride.getPassengerId() != null)
        .filter(ride -> passengerId.equals(ride.getPassengerId()))
        .toList();

    log.debug("Found {} rides for passengerId={}", rides.size(), passengerId);
    return rides;
  }

  @Override
  public boolean existsActiveByPassengerId(UUID passengerId) {
    if (passengerId == null) {
      throw new IllegalArgumentException("ID missing!");
    }

    boolean exists = findAllByPassengerId(passengerId)
        .stream()
        .filter(ride -> ride.getStatus() != null)
        .anyMatch(ride -> ACTIVE_RIDE_STATES.contains(ride.getStatus()));

    log.debug("Active ride exists for passengerId={}: {}", passengerId, exists);
    return exists;
  }

  @Override
  public boolean existsActiveByDriverId(UUID driverId) {
    if (driverId == null) {
      throw new IllegalArgumentException("ID missing!");
    }

    boolean exists = findAllByDriverId(driverId)
        .stream()
        .filter(ride -> ride.getStatus() != null)
        .anyMatch(ride -> ACTIVE_RIDE_STATES.contains(ride.getStatus()));

    log.debug("Active ride exists for driverId={}: {}", driverId, exists);
    return exists;
  }

}
