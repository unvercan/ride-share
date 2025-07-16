package tr.unvercanunlu.ride_share.service.impl;

import java.util.UUID;
import tr.unvercanunlu.ride_share.dao.DriverRepository;
import tr.unvercanunlu.ride_share.dto.request.RegisterDriverDto;
import tr.unvercanunlu.ride_share.dto.request.UpdateLocationDto;
import tr.unvercanunlu.ride_share.entity.Driver;
import tr.unvercanunlu.ride_share.exception.DriverNotFoundException;
import tr.unvercanunlu.ride_share.service.DriverService;
import tr.unvercanunlu.ride_share.service.ValidationService;
import tr.unvercanunlu.ride_share.status.DriverStatus;

public class DriverServiceImpl implements DriverService {

  private final DriverRepository driverRepository;
  private final ValidationService validationService;

  public DriverServiceImpl(DriverRepository driverRepository, ValidationService validationService) {
    this.driverRepository = driverRepository;
    this.validationService = validationService;
  }

  @Override
  public Driver register(RegisterDriverDto request) {
    Driver driver = Driver.of(request);
    return driverRepository.save(driver);
  }

  @Override
  public Driver updateLocation(UpdateLocationDto request) {
    Driver driver = getDetail(request.driverId());
    driver.setCurrent(request.current());
    return driverRepository.save(driver);
  }

  @Override
  public Driver makeOffline(UUID driverId) {
    Driver driver = getDetail(driverId);
    validationService.checkActiveRideForDriver(driverId);
    driver.setStatus(DriverStatus.OFFLINE);
    return driverRepository.save(driver);
  }

  @Override
  public Driver makeAvailable(UUID driverId) {
    Driver driver = getDetail(driverId);
    validationService.checkActiveRideForDriver(driverId);
    driver.setStatus(DriverStatus.AVAILABLE);
    return driverRepository.save(driver);
  }

  @Override
  public Driver getDetail(UUID driverId) throws DriverNotFoundException {
    if (driverId == null) {
      throw new IllegalArgumentException("Driver ID missing");
    }

    return driverRepository.get(driverId).orElseThrow(() -> new DriverNotFoundException(driverId));
  }

}
