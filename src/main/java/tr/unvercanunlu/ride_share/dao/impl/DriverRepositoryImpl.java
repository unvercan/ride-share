package tr.unvercanunlu.ride_share.dao.impl;

import java.util.UUID;
import tr.unvercanunlu.ride_share.core.impl.InMemoryDaoImpl;
import tr.unvercanunlu.ride_share.dao.DriverRepository;
import tr.unvercanunlu.ride_share.entity.Driver;
import tr.unvercanunlu.ride_share.helper.LogHelper;
import tr.unvercanunlu.ride_share.status.DriverStatus;

public class DriverRepositoryImpl extends InMemoryDaoImpl<Driver> implements DriverRepository {

  @Override
  public boolean checkAvailable(UUID driverId) {
    boolean available = (driverId != null) && entities.containsKey(driverId) && DriverStatus.AVAILABLE.equals(entities.get(driverId).getStatus());

    LogHelper.debug(this.getClass(),
        String.format("Checked availability for driverId=%s. Available=%b", driverId, available));

    return available;
  }

  @Override
  public void updateAsAvailable(UUID driverId) {
    if ((driverId != null) && entities.containsKey(driverId)) {
      entities.get(driverId).setStatus(DriverStatus.AVAILABLE);

      LogHelper.info(this.getClass(),
          String.format("Updated driver as AVAILABLE. driverId=%s", driverId));

    } else {
      LogHelper.error(this.getClass(),
          String.format("Failed to update as AVAILABLE. driverId=%s not found.", driverId));
    }
  }

  @Override
  public void updateAsBusy(UUID driverId) {
    if ((driverId != null) && entities.containsKey(driverId)) {
      entities.get(driverId).setStatus(DriverStatus.BUSY);

      LogHelper.info(this.getClass(),
          String.format("Updated driver as BUSY. driverId=%s", driverId));

    } else {
      LogHelper.error(this.getClass(),
          String.format("Failed to update as BUSY. driverId=%s not found.", driverId));
    }
  }

  @Override
  public boolean checkExists(UUID driverId) {
    boolean exists = (driverId != null) && entities.containsKey(driverId);

    LogHelper.debug(this.getClass(),
        String.format("Checked existence for driverId=%s. Exists=%b", driverId, exists));

    return exists;
  }

}
