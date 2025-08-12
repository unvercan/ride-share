package tr.unvercanunlu.ride_share.dao;

import java.util.UUID;
import tr.unvercanunlu.ride_share.core.dao.Dao;
import tr.unvercanunlu.ride_share.entity.Driver;

public interface DriverRepository extends Dao<Driver, UUID> {

  boolean isAvailable(UUID driverId);

  void setAvailable(UUID driverId);

  void setBusy(UUID driverId);

  boolean existsById(UUID driverId);

}
