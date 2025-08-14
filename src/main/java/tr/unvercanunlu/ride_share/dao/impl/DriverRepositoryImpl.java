package tr.unvercanunlu.ride_share.dao.impl;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import tr.unvercanunlu.ride_share.core.dao.impl.InMemoryDaoImpl;
import tr.unvercanunlu.ride_share.dao.DriverRepository;
import tr.unvercanunlu.ride_share.entity.Driver;
import tr.unvercanunlu.ride_share.status.DriverStatus;
import tr.unvercanunlu.ride_share.util.ValidationUtil;

@Slf4j
public class DriverRepositoryImpl extends InMemoryDaoImpl<Driver> implements DriverRepository {

  @Override
  public boolean isAvailable(UUID driverId) {
    ValidationUtil.checkIdNotNull(driverId);
    boolean available = entities.containsKey(driverId)
        && DriverStatus.AVAILABLE.equals(entities.get(driverId).getStatus());
    log.debug("Checked availability for driverId={}. Available={}", driverId, available);
    return available;
  }

  @Override
  public void setAvailable(UUID driverId) {
    ValidationUtil.checkIdNotNull(driverId);
    if (entities.containsKey(driverId)) {
      entities.get(driverId).setStatus(DriverStatus.AVAILABLE);
      log.info("Updated driver as AVAILABLE. driverId={}", driverId);
    } else {
      log.error("Failed to update as AVAILABLE. driverId={} not found.", driverId);
    }
  }

  @Override
  public void setBusy(UUID driverId) {
    ValidationUtil.checkIdNotNull(driverId);
    if (entities.containsKey(driverId)) {
      entities.get(driverId).setStatus(DriverStatus.BUSY);
      log.info("Updated driver as BUSY. driverId={}", driverId);
    } else {
      log.error("Failed to update as BUSY. driverId={} not found.", driverId);
    }
  }

  @Override
  public boolean existsById(UUID driverId) {
    ValidationUtil.checkIdNotNull(driverId);
    boolean exists = entities.containsKey(driverId);
    log.debug("Checked existence for driverId={}. Exists={}", driverId, exists);
    return exists;
  }

}
