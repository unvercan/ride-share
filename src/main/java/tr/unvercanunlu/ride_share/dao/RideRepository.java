package tr.unvercanunlu.ride_share.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import tr.unvercanunlu.ride_share.core.Dao;
import tr.unvercanunlu.ride_share.entity.Ride;

public interface RideRepository extends Dao<Ride, UUID> {

  List<Ride> getRequestedRidesBetweenGap(LocalDateTime gapStart, LocalDateTime gapEnd);

  List<Ride> getByDriver(UUID driverId);

  List<Ride> getByPassenger(UUID passengerId);

  boolean checkActiveRideForPassenger(UUID passengerId);

  boolean checkActiveRideForDriver(UUID driverId);

}
