package tr.unvercanunlu.ride_share.service.impl;

import java.util.UUID;
import tr.unvercanunlu.ride_share.dao.DriverRepository;
import tr.unvercanunlu.ride_share.dao.RideRepository;
import tr.unvercanunlu.ride_share.dto.request.RegisterDriverDto;
import tr.unvercanunlu.ride_share.entity.Driver;
import tr.unvercanunlu.ride_share.entity.Location;
import tr.unvercanunlu.ride_share.exception.DriverHasActiveRideException;
import tr.unvercanunlu.ride_share.exception.DriverNotFoundException;
import tr.unvercanunlu.ride_share.service.DriverService;
import tr.unvercanunlu.ride_share.status.DriverStatus;

public class DriverServiceImpl implements DriverService {

  private final DriverRepository driverRepository;
  private final RideRepository rideRepository;

  public DriverServiceImpl(
      DriverRepository driverRepository,
      RideRepository rideRepository
  ) {
    this.driverRepository = driverRepository;
    this.rideRepository = rideRepository;
  }

  @Override
  public Driver register(RegisterDriverDto request) {
    Driver driver = new Driver();
    driver.setName(request.name());
    driver.setEmail(request.email());
    driver.setPhone(request.phone());
    driver.setPlate(request.plate());
    driver.setStatus(DriverStatus.OFFLINE);

    return driverRepository.save(driver);
  }

  @Override
  public Driver updateLocation(UUID driverId, Location current) throws DriverNotFoundException {
    Driver driver = getDetail(driverId);

    driver.setCurrent(current);

    return driverRepository.save(driver);
  }

  @Override
  public Driver makeOffline(UUID driverId) throws DriverNotFoundException, DriverHasActiveRideException {
    Driver driver = getDetail(driverId);

    if (rideRepository.checkActiveRideForDriver(driverId)) {
      throw new DriverHasActiveRideException(driverId);
    }

    driver.setStatus(DriverStatus.OFFLINE);

    return driverRepository.save(driver);
  }

  @Override
  public Driver makeAvailable(UUID driverId) throws DriverNotFoundException {
    Driver driver = getDetail(driverId);

    if (rideRepository.checkActiveRideForDriver(driverId)) {
      throw new DriverHasActiveRideException(driverId);
    }

    driver.setStatus(DriverStatus.AVAILABLE);

    return driverRepository.save(driver);
  }

  @Override
  public Driver getDetail(UUID driverId) throws DriverNotFoundException {
    return driverRepository.get(driverId)
        .orElseThrow(() -> new DriverNotFoundException(driverId));
  }

}
