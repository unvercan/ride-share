package tr.unvercanunlu.ride_share.dao.impl;

import java.util.UUID;
import tr.unvercanunlu.ride_share.core.impl.InMemoryDaoImpl;
import tr.unvercanunlu.ride_share.dao.DriverRepository;
import tr.unvercanunlu.ride_share.entity.Driver;
import tr.unvercanunlu.ride_share.status.DriverStatus;

public class DriverRepositoryImpl extends InMemoryDaoImpl<Driver> implements DriverRepository {

  @Override
  public boolean checkAvailable(UUID driverId) {
    return entities.values()
        .stream()
        .anyMatch(driver -> driver.getId().equals(driverId) && driver.getStatus().equals(DriverStatus.AVAILABLE));
  }

  @Override
  public void updateAsAvailable(UUID driverId) {
    if (entities.containsKey(driverId)) {
      entities.get(driverId).setStatus(DriverStatus.AVAILABLE);
    }
  }

  @Override
  public void updateAsBusy(UUID driverId) {
    if (entities.containsKey(driverId)) {
      entities.get(driverId).setStatus(DriverStatus.BUSY);
    }
  }

  @Override
  public boolean checkExists(UUID driverId) {
    return entities.values()
        .stream()
        .anyMatch(driver -> driver.getId().equals(driverId));
  }

}
