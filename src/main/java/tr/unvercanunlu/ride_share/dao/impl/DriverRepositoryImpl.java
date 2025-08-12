package tr.unvercanunlu.ride_share.dao.impl;

import java.util.UUID;
import tr.unvercanunlu.ride_share.core.dao.impl.InMemoryDaoImpl;
import tr.unvercanunlu.ride_share.core.log.Logger;
import tr.unvercanunlu.ride_share.dao.DriverRepository;
import tr.unvercanunlu.ride_share.entity.Driver;
import tr.unvercanunlu.ride_share.helper.LoggerFactory;
import tr.unvercanunlu.ride_share.status.DriverStatus;

public class DriverRepositoryImpl extends InMemoryDaoImpl<Driver> implements DriverRepository {

  private static final Logger logger = LoggerFactory.getLogger(DriverRepositoryImpl.class);

  @Override
  public boolean isAvailable(UUID driverId) {
    boolean available = (driverId != null) && entities.containsKey(driverId) && DriverStatus.AVAILABLE.equals(entities.get(driverId).getStatus());
    logger.debug(String.format("Checked availability for driverId=%s. Available=%b", driverId, available));
    return available;
  }

  @Override
  public void setAvailable(UUID driverId) {
    if ((driverId != null) && entities.containsKey(driverId)) {
      entities.get(driverId).setStatus(DriverStatus.AVAILABLE);
      logger.info(String.format("Updated driver as AVAILABLE. driverId=%s", driverId));
    } else {
      logger.error(String.format("Failed to update as AVAILABLE. driverId=%s not found.", driverId));
    }
  }

  @Override
  public void setBusy(UUID driverId) {
    if ((driverId != null) && entities.containsKey(driverId)) {
      entities.get(driverId).setStatus(DriverStatus.BUSY);
      logger.info(String.format("Updated driver as BUSY. driverId=%s", driverId));
    } else {
      logger.error(String.format("Failed to update as BUSY. driverId=%s not found.", driverId));
    }
  }

  @Override
  public boolean existsById(UUID driverId) {
    boolean exists = (driverId != null) && entities.containsKey(driverId);
    logger.debug(String.format("Checked existence for driverId=%s. Exists=%b", driverId, exists));
    return exists;
  }

}
