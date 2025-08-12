package tr.unvercanunlu.ride_share.dao;

import java.util.UUID;
import tr.unvercanunlu.ride_share.core.dao.Dao;
import tr.unvercanunlu.ride_share.entity.Passenger;

public interface PassengerRepository extends Dao<Passenger, UUID> {

  boolean existsById(UUID passengerId);

}
