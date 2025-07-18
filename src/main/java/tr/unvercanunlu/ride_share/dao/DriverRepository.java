package tr.unvercanunlu.ride_share.dao;

import java.util.UUID;
import tr.unvercanunlu.ride_share.core.Dao;
import tr.unvercanunlu.ride_share.entity.Driver;

public interface DriverRepository extends Dao<Driver, UUID> {

  boolean checkAvailable(UUID driverId);

  void updateAsAvailable(UUID driverId);

  void updateAsBusy(UUID driverId);

  boolean checkExists(UUID driverId);

}
