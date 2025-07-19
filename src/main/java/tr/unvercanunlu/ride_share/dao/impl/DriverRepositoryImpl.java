package tr.unvercanunlu.ride_share.dao.impl;

import java.util.UUID;
import tr.unvercanunlu.ride_share.core.impl.InMemoryDaoImpl;
import tr.unvercanunlu.ride_share.dao.DriverRepository;
import tr.unvercanunlu.ride_share.entity.Driver;
import tr.unvercanunlu.ride_share.status.DriverStatus;

public class DriverRepositoryImpl extends InMemoryDaoImpl<Driver> implements DriverRepository {

  @Override
  public boolean checkAvailable(UUID driverId) {
    return (driverId != null) && entities.containsKey(driverId) && DriverStatus.AVAILABLE.equals(entities.get(driverId).getStatus());
  }

  @Override
  public void updateAsAvailable(UUID driverId) {
    if ((driverId != null) && entities.containsKey(driverId)) {
      entities.get(driverId).setStatus(DriverStatus.AVAILABLE);
    }
  }

  @Override
  public void updateAsBusy(UUID driverId) {
    if ((driverId != null) && entities.containsKey(driverId)) {
      entities.get(driverId).setStatus(DriverStatus.BUSY);
    }
  }

  @Override
  public boolean checkExists(UUID driverId) {
    return (driverId != null) && entities.containsKey(driverId);
  }

}
