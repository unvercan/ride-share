package tr.unvercanunlu.ride_share.service.impl;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import tr.unvercanunlu.ride_share.core.log.Logger;
import tr.unvercanunlu.ride_share.dao.DriverRepository;
import tr.unvercanunlu.ride_share.dto.request.RegisterDriverDto;
import tr.unvercanunlu.ride_share.dto.request.UpdateLocationDto;
import tr.unvercanunlu.ride_share.entity.Driver;
import tr.unvercanunlu.ride_share.entity.EntityFactory;
import tr.unvercanunlu.ride_share.exception.NotFoundException;
import tr.unvercanunlu.ride_share.helper.LoggerFactory;
import tr.unvercanunlu.ride_share.service.DriverService;
import tr.unvercanunlu.ride_share.service.ValidationService;
import tr.unvercanunlu.ride_share.status.DriverStatus;

@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

  private static final Logger logger = LoggerFactory.getLogger(DriverServiceImpl.class);

  private final DriverRepository driverRepository;
  private final ValidationService validationService;

  @Override
  public Driver register(RegisterDriverDto request) {
    logger.info(String.format("Registering new driver: email=%s", request.email()));

    try {
      Driver driver = EntityFactory.from(request);
      driver = driverRepository.save(driver);
      logger.info(String.format("Driver successfully registered: email=%s, id=%s", driver.getEmail(), driver.getId()));
      return driver;
    } catch (Exception e) {
      logger.error(String.format("Failed to register driver: email=%s, error=%s", request.email(), e.getMessage()), e);
      throw e;
    }
  }

  @Override
  public Driver updateLocation(UpdateLocationDto request) {
    logger.info(String.format("Updating driver location: driverId=%s", request.driverId()));

    try {
      Driver driver = getDriver(request.driverId());
      driver.setCurrent(request.current());
      driver = driverRepository.save(driver);
      logger.info(String.format("Driver location updated: driverId=%s, location=%s", driver.getId(), driver.getCurrent()));
      return driver;

    } catch (Exception e) {
      logger.error(String.format("Failed to update driver location: driverId=%s, error=%s", request.driverId(), e.getMessage()), e);
      throw e;
    }
  }

  @Override
  public Driver setOffline(UUID driverId) {
    logger.info(String.format("Setting driver to offline: driverId=%s", driverId));

    try {
      Driver driver = getDriver(driverId);
      validationService.checkNoActiveRideForDriver(driverId);
      driver.setStatus(DriverStatus.OFFLINE);
      driver = driverRepository.save(driver);
      logger.info(String.format("Driver set to offline: driverId=%s", driverId));
      return driver;
    } catch (Exception e) {
      logger.error(String.format("Failed to set driver offline: driverId=%s, error=%s", driverId, e.getMessage()), e);
      throw e;
    }
  }

  @Override
  public Driver setAvailable(UUID driverId) {
    logger.info(String.format("Setting driver to available: driverId=%s", driverId));

    try {
      Driver driver = getDriver(driverId);
      validationService.checkNoActiveRideForDriver(driverId);
      driver.setStatus(DriverStatus.AVAILABLE);
      driver = driverRepository.save(driver);
      logger.info(String.format("Driver set to available: driverId=%s", driverId));
      return driver;
    } catch (Exception e) {
      logger.error(String.format("Failed to set driver available: driverId=%s, error=%s", driverId, e.getMessage()), e);
      throw e;
    }
  }

  @Override
  public Driver getDriver(UUID driverId) throws NotFoundException {
    logger.info(String.format("Retrieving driver details: driverId=%s", driverId));

    try {
      validationService.checkIdentifier(driverId, Driver.class);
      Driver driver = driverRepository.findById(driverId).orElseThrow(() -> new NotFoundException(Driver.class, driverId));
      logger.info(String.format("Driver details retrieved: driverId=%s", driverId));
      return driver;
    } catch (Exception e) {
      logger.error(String.format("Failed to retrieve driver details: driverId=%s, error=%s", driverId, e.getMessage()), e);
      throw e;
    }
  }

}
