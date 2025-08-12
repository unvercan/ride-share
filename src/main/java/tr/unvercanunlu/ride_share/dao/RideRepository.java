package tr.unvercanunlu.ride_share.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import tr.unvercanunlu.ride_share.core.dao.Dao;
import tr.unvercanunlu.ride_share.entity.Ride;

public interface RideRepository extends Dao<Ride, UUID> {

  List<Ride> findRequestedWithinWindow(LocalDateTime windowStart, LocalDateTime windowEnd);

  List<Ride> findAllByDriverId(UUID driverId);

  List<Ride> findAllByPassengerId(UUID passengerId);

  boolean existsActiveByPassengerId(UUID passengerId);

  boolean existsActiveByDriverId(UUID driverId);

}
