package tr.unvercanunlu.ride_share.service.impl;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tr.unvercanunlu.ride_share.dao.DriverRepository;
import tr.unvercanunlu.ride_share.dto.request.RegisterDriverDto;
import tr.unvercanunlu.ride_share.dto.request.UpdateLocationDto;
import tr.unvercanunlu.ride_share.entity.Driver;
import tr.unvercanunlu.ride_share.entity.EntityFactory;
import tr.unvercanunlu.ride_share.exception.NotFoundException;
import tr.unvercanunlu.ride_share.service.DriverService;
import tr.unvercanunlu.ride_share.service.ValidationService;
import tr.unvercanunlu.ride_share.status.DriverStatus;

@Slf4j
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

  private final DriverRepository driverRepository;
  private final ValidationService validationService;

  @Override
  public Driver register(RegisterDriverDto request) {
    log.info("Registering new driver: email={}", request.email());
    try {
      Driver driver = EntityFactory.from(request);
      driver = driverRepository.save(driver);
      log.info("Driver successfully registered: email={}, id={}", driver.getEmail(), driver.getId());
      return driver;
    } catch (Exception ex) {
      log.error("Failed to register driver: email={}", request.email(), ex);
      throw ex;
    }
  }

  @Override
  public Driver updateLocation(UpdateLocationDto request) {
    log.info("Updating driver location: driverId={}", request.driverId());
    try {
      Driver driver = getDriver(request.driverId());
      driver.setCurrent(request.current());
      driver = driverRepository.save(driver);
      log.info("Driver location updated: driverId={}, location={}", driver.getId(), driver.getCurrent());
      return driver;
    } catch (Exception ex) {
      log.error("Failed to update driver location: driverId={}", request.driverId(), ex);
      throw ex;
    }
  }

  @Override
  public Driver setOffline(UUID driverId) {
    log.info("Setting driver to offline: driverId={}", driverId);
    try {
      Driver driver = getDriver(driverId);
      validationService.checkNoActiveRideForDriver(driverId);
      driver.setStatus(DriverStatus.OFFLINE);
      driver = driverRepository.save(driver);
      log.info("Driver set to offline: driverId={}", driverId);
      return driver;
    } catch (Exception ex) {
      log.error("Failed to set driver offline: driverId={}", driverId, ex);
      throw ex;
    }
  }

  @Override
  public Driver setAvailable(UUID driverId) {
    log.info("Setting driver to available: driverId={}", driverId);
    try {
      Driver driver = getDriver(driverId);
      validationService.checkNoActiveRideForDriver(driverId);
      driver.setStatus(DriverStatus.AVAILABLE);
      driver = driverRepository.save(driver);
      log.info("Driver set to available: driverId={}", driverId);
      return driver;
    } catch (Exception ex) {
      log.error("Failed to set driver available: driverId={}", driverId, ex);
      throw ex;
    }
  }

  @Override
  public Driver getDriver(UUID driverId) throws NotFoundException {
    log.info("Retrieving driver details: driverId={}", driverId);
    try {
      validationService.checkIdentifier(driverId, Driver.class);
      Driver driver = driverRepository.findById(driverId).orElseThrow(() -> new NotFoundException(Driver.class, driverId));
      log.info("Driver details retrieved: driverId={}", driverId);
      return driver;
    } catch (Exception ex) {
      log.error("Failed to retrieve driver details: driverId={}", driverId, ex);
      throw ex;
    }
  }

}
