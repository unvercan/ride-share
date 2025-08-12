package tr.unvercanunlu.ride_share.dao.impl;

import static tr.unvercanunlu.ride_share.config.AppConfig.ACTIVE_RIDE_STATES;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import tr.unvercanunlu.ride_share.core.dao.impl.InMemoryDaoImpl;
import tr.unvercanunlu.ride_share.core.log.Logger;
import tr.unvercanunlu.ride_share.dao.RideRepository;
import tr.unvercanunlu.ride_share.entity.Ride;
import tr.unvercanunlu.ride_share.helper.LoggerFactory;
import tr.unvercanunlu.ride_share.status.RideStatus;

public class RideRepositoryImpl extends InMemoryDaoImpl<Ride> implements RideRepository {

  private static final Logger logger = LoggerFactory.getLogger(RideRepositoryImpl.class);

  @Override
  public List<Ride> findRequestedWithinWindow(LocalDateTime windowStart, LocalDateTime windowEnd) {
    logger.debug("Getting requested rides between %s and %s".formatted(windowStart, windowEnd));
    if ((windowStart == null) || (windowEnd == null)) {
      logger.error("Gap start or end is null!");
      return List.of();
    }

    List<Ride> rides = entities.values()
        .stream()
        .filter(ride -> ride.getStatus() != null)
        .filter(ride -> ride.getRequestedAt() != null)
        .filter(ride -> RideStatus.REQUESTED.equals(ride.getStatus()))
        .filter(ride -> !ride.getRequestedAt().isBefore(windowStart) && !ride.getRequestedAt().isAfter(windowEnd))
        .toList();

    logger.debug("Found %d requested rides in gap".formatted(rides.size()));
    return rides;
  }

  @Override
  public List<Ride> findAllByDriverId(UUID driverId) {
    logger.debug("Getting rides by driverId=%s".formatted(driverId));
    if (driverId == null) {
      logger.error("driverId is null!");
      return List.of();
    }

    List<Ride> rides = entities.values()
        .stream()
        .filter(ride -> ride.getDriverId() != null)
        .filter(ride -> driverId.equals(ride.getDriverId()))
        .toList();

    logger.debug("Found %d rides for driverId=%s".formatted(rides.size(), driverId));
    return rides;
  }

  @Override
  public List<Ride> findAllByPassengerId(UUID passengerId) {
    logger.debug("Getting rides by passengerId=%s".formatted(passengerId));
    if (passengerId == null) {
      logger.error("passengerId is null!");
      return List.of();
    }

    List<Ride> rides = entities.values()
        .stream()
        .filter(ride -> ride.getPassengerId() != null)
        .filter(ride -> passengerId.equals(ride.getPassengerId()))
        .toList();

    logger.debug("Found %d rides for passengerId=%s".formatted(rides.size(), passengerId));
    return rides;
  }

  @Override
  public boolean existsActiveByPassengerId(UUID passengerId) {
    boolean exists = (passengerId != null)
        && findAllByPassengerId(passengerId)
        .stream()
        .filter(ride -> ride.getStatus() != null)
        .anyMatch(ride -> ACTIVE_RIDE_STATES.contains(ride.getStatus()));

    logger.debug("Active ride exists for passengerId=%s: %b".formatted(passengerId, exists));
    return exists;
  }

  @Override
  public boolean existsActiveByDriverId(UUID driverId) {
    boolean exists = (driverId != null)
        && findAllByDriverId(driverId)
        .stream()
        .filter(ride -> ride.getStatus() != null)
        .anyMatch(ride -> ACTIVE_RIDE_STATES.contains(ride.getStatus()));

    logger.debug("Active ride exists for driverId=%s: %b".formatted(driverId, exists));
    return exists;
  }

}
