package tr.unvercanunlu.ride_share.service.impl;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import tr.unvercanunlu.ride_share.dao.DriverRepository;
import tr.unvercanunlu.ride_share.dto.request.RegisterDriverDto;
import tr.unvercanunlu.ride_share.dto.request.UpdateLocationDto;
import tr.unvercanunlu.ride_share.entity.Driver;
import tr.unvercanunlu.ride_share.entity.EntityFactory;
import tr.unvercanunlu.ride_share.exception.NotFoundException;
import tr.unvercanunlu.ride_share.helper.LogHelper;
import tr.unvercanunlu.ride_share.service.DriverService;
import tr.unvercanunlu.ride_share.service.ValidationService;
import tr.unvercanunlu.ride_share.status.DriverStatus;

@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

  private final DriverRepository driverRepository;
  private final ValidationService validationService;

  @Override
  public Driver register(RegisterDriverDto request) {
    LogHelper.info(this.getClass(),
        String.format("Registering new driver: email=%s", request.email()));

    try {
      Driver driver = EntityFactory.from(request);

      driver = driverRepository.save(driver);

      LogHelper.info(this.getClass(),
          String.format("Driver successfully registered: email=%s, id=%s", driver.getEmail(), driver.getId()));

      return driver;

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Failed to register driver: email=%s, error=%s", request.email(), e.getMessage()), e);

      throw e;
    }
  }

  @Override
  public Driver updateLocation(UpdateLocationDto request) {
    LogHelper.info(this.getClass(),
        String.format("Updating driver location: driverId=%s", request.driverId()));

    try {
      Driver driver = getDetail(request.driverId());

      driver.setCurrent(request.current());
      driver = driverRepository.save(driver);

      LogHelper.info(this.getClass(),
          String.format("Driver location updated: driverId=%s, location=%s", driver.getId(), driver.getCurrent()));

      return driver;

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Failed to update driver location: driverId=%s, error=%s", request.driverId(), e.getMessage()), e);

      throw e;
    }
  }

  @Override
  public Driver makeOffline(UUID driverId) {
    LogHelper.info(this.getClass(),
        String.format("Setting driver to offline: driverId=%s", driverId));

    try {
      Driver driver = getDetail(driverId);

      validationService.checkNoActiveRideForDriver(driverId);
      driver.setStatus(DriverStatus.OFFLINE);
      driver = driverRepository.save(driver);

      LogHelper.info(this.getClass(),
          String.format("Driver set to offline: driverId=%s", driverId));

      return driver;

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Failed to set driver offline: driverId=%s, error=%s", driverId, e.getMessage()), e);

      throw e;
    }
  }

  @Override
  public Driver makeAvailable(UUID driverId) {
    LogHelper.info(this.getClass(),
        String.format("Setting driver to available: driverId=%s", driverId));

    try {
      Driver driver = getDetail(driverId);

      validationService.checkNoActiveRideForDriver(driverId);
      driver.setStatus(DriverStatus.AVAILABLE);
      driver = driverRepository.save(driver);

      LogHelper.info(this.getClass(),
          String.format("Driver set to available: driverId=%s", driverId));

      return driver;

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Failed to set driver available: driverId=%s, error=%s", driverId, e.getMessage()), e);

      throw e;
    }
  }

  @Override
  public Driver getDetail(UUID driverId) throws NotFoundException {
    LogHelper.info(this.getClass(),
        String.format("Retrieving driver details: driverId=%s", driverId));

    try {
      validationService.checkIdentifier(driverId, Driver.class);
      Driver driver = driverRepository.get(driverId)
          .orElseThrow(() -> new NotFoundException(Driver.class, driverId));

      LogHelper.info(this.getClass(),
          String.format("Driver details retrieved: driverId=%s", driverId));

      return driver;

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Failed to retrieve driver details: driverId=%s, error=%s", driverId, e.getMessage()), e);

      throw e;
    }
  }

}
