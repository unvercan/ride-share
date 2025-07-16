package tr.unvercanunlu.ride_share.dao;

import java.util.UUID;
import tr.unvercanunlu.ride_share.core.Dao;
import tr.unvercanunlu.ride_share.entity.Driver;

public interface DriverRepository extends Dao<Driver, UUID> {

  boolean checkDriverAvailable(UUID driverId);
}
