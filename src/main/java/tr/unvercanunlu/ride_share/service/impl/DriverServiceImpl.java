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
    logger.info("Registering new driver: email=%s".formatted(request.email()));

    try {
      Driver driver = EntityFactory.from(request);
      driver = driverRepository.save(driver);
      logger.info("Driver successfully registered: email=%s, id=%s".formatted(driver.getEmail(), driver.getId()));
      return driver;
    } catch (Exception e) {
      logger.error("Failed to register driver: email=%s, error=%s".formatted(request.email(), e.getMessage()), e);
      throw e;
    }
  }

  @Override
  public Driver updateLocation(UpdateLocationDto request) {
    logger.info("Updating driver location: driverId=%s".formatted(request.driverId()));

    try {
      Driver driver = getDriver(request.driverId());
      driver.setCurrent(request.current());
      driver = driverRepository.save(driver);
      logger.info("Driver location updated: driverId=%s, location=%s".formatted(driver.getId(), driver.getCurrent()));
      return driver;

    } catch (Exception e) {
      logger.error("Failed to update driver location: driverId=%s, error=%s".formatted(request.driverId(), e.getMessage()), e);
      throw e;
    }
  }

  @Override
  public Driver setOffline(UUID driverId) {
    logger.info("Setting driver to offline: driverId=%s".formatted(driverId));

    try {
      Driver driver = getDriver(driverId);
      validationService.checkNoActiveRideForDriver(driverId);
      driver.setStatus(DriverStatus.OFFLINE);
      driver = driverRepository.save(driver);
      logger.info("Driver set to offline: driverId=%s".formatted(driverId));
      return driver;
    } catch (Exception e) {
      logger.error("Failed to set driver offline: driverId=%s, error=%s".formatted(driverId, e.getMessage()), e);
      throw e;
    }
  }

  @Override
  public Driver setAvailable(UUID driverId) {
    logger.info("Setting driver to available: driverId=%s".formatted(driverId));

    try {
      Driver driver = getDriver(driverId);
      validationService.checkNoActiveRideForDriver(driverId);
      driver.setStatus(DriverStatus.AVAILABLE);
      driver = driverRepository.save(driver);
      logger.info("Driver set to available: driverId=%s".formatted(driverId));
      return driver;
    } catch (Exception e) {
      logger.error("Failed to set driver available: driverId=%s, error=%s".formatted(driverId, e.getMessage()), e);
      throw e;
    }
  }

  @Override
  public Driver getDriver(UUID driverId) throws NotFoundException {
    logger.info("Retrieving driver details: driverId=%s".formatted(driverId));

    try {
      validationService.checkIdentifier(driverId, Driver.class);
      Driver driver = driverRepository.findById(driverId).orElseThrow(() -> new NotFoundException(Driver.class, driverId));
      logger.info("Driver details retrieved: driverId=%s".formatted(driverId));
      return driver;
    } catch (Exception e) {
      logger.error("Failed to retrieve driver details: driverId=%s, error=%s".formatted(driverId, e.getMessage()), e);
      throw e;
    }
  }

}
