package tr.unvercanunlu.ride_share.service.impl;

import java.util.Optional;
import java.util.UUID;
import tr.unvercanunlu.ride_share.dao.IDriverDao;
import tr.unvercanunlu.ride_share.dao.IRideDao;
import tr.unvercanunlu.ride_share.dao.impl.DriverDao;
import tr.unvercanunlu.ride_share.dao.impl.RideDao;
import tr.unvercanunlu.ride_share.dto.request.RegisterDriverDto;
import tr.unvercanunlu.ride_share.entity.Driver;
import tr.unvercanunlu.ride_share.entity.Location;
import tr.unvercanunlu.ride_share.exception.DriverHasActiveRideException;
import tr.unvercanunlu.ride_share.exception.DriverNotFoundException;
import tr.unvercanunlu.ride_share.service.IDriverService;
import tr.unvercanunlu.ride_share.status.DriverStatus;

public class DriverService implements IDriverService {

  private final IDriverDao driverDao;
  private final IRideDao rideDao;

  public DriverService(IDriverDao driverDao, IRideDao rideDao) {
    this.driverDao = driverDao;
    this.rideDao = rideDao;
  }

  @Override
  public Driver register(RegisterDriverDto request) {
    Driver driver = new Driver();
    driver.setId(UUID.randomUUID());
    driver.setName(request.name());
    driver.setEmail(request.email());
    driver.setPhone(request.phone());
    driver.setPlate(request.plate());
    driver.setStatus(DriverStatus.OFFLINE);

    return driverDao.save(driver);
  }

  @Override
  public Driver updateLocation(UUID driverId, Location current) throws DriverNotFoundException {
    Driver driver = Optional.ofNullable(
        driverDao.get(driverId)
    ).orElseThrow(() -> new DriverNotFoundException(driverId));

    driver.setCurrent(current);

    return driverDao.save(driver);
  }

  @Override
  public Driver makeOffline(UUID driverId) throws DriverNotFoundException, DriverHasActiveRideException {
    Driver driver = Optional.ofNullable(
        driverDao.get(driverId)
    ).orElseThrow(() -> new DriverNotFoundException(driverId));

    if (rideDao.checkActiveRideForDriver(driverId)) {
      throw new DriverHasActiveRideException(driverId);
    }

    driver.setStatus(DriverStatus.OFFLINE);

    return driverDao.save(driver);
  }

  @Override
  public Driver makeAvailable(UUID driverId) throws DriverNotFoundException {
    Driver driver = Optional.ofNullable(
        driverDao.get(driverId)
    ).orElseThrow(() -> new DriverNotFoundException(driverId));

    driver.setStatus(DriverStatus.AVAILABLE);

    return driverDao.save(driver);
  }

  @Override
  public Driver getDetail(UUID driverId) throws DriverNotFoundException {
    return Optional.ofNullable(
        driverDao.get(driverId)
    ).orElseThrow(() -> new DriverNotFoundException(driverId));
  }

}
