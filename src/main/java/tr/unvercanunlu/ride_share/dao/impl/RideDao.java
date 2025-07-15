package tr.unvercanunlu.ride_share.dao.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import tr.unvercanunlu.ride_share.core.BaseDao;
import tr.unvercanunlu.ride_share.dao.IRideDao;
import tr.unvercanunlu.ride_share.entity.Ride;
import tr.unvercanunlu.ride_share.status.RideStatus;

public class RideDao extends BaseDao<Ride> implements IRideDao {

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
        .filter(ride -> ride.getDriverId().equals(driverId))
        .toList();
  }

  @Override
  public List<Ride> getByPassenger(UUID passengerId) {
    return entities.values()
        .stream()
        .filter(ride -> ride.getPassengerId().equals(passengerId))
        .toList();
  }

}
