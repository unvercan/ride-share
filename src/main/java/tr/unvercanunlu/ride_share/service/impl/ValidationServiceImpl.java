package tr.unvercanunlu.ride_share.service.impl;

import java.util.UUID;
import tr.unvercanunlu.ride_share.core.BaseEntity;
import tr.unvercanunlu.ride_share.dao.DriverRepository;
import tr.unvercanunlu.ride_share.dao.RideRepository;
import tr.unvercanunlu.ride_share.exception.DriverHasActiveRideException;
import tr.unvercanunlu.ride_share.exception.DriverUnavailableException;
import tr.unvercanunlu.ride_share.exception.IdentifierMissingException;
import tr.unvercanunlu.ride_share.exception.PassengerHasActiveRideException;
import tr.unvercanunlu.ride_share.service.ValidationService;

public class ValidationServiceImpl implements ValidationService {

  private final RideRepository rideRepository;
  private final DriverRepository driverRepository;

  public ValidationServiceImpl(RideRepository rideRepository, DriverRepository driverRepository) {
    this.rideRepository = rideRepository;
    this.driverRepository = driverRepository;
  }

  @Override
  public void checkActiveRideForDriver(UUID driverId) throws DriverHasActiveRideException {
    if (rideRepository.checkActiveRideForDriver(driverId)) {
      throw new DriverHasActiveRideException(driverId);
    }
  }

  @Override
  public void checkActiveRideForPassenger(UUID passengerId) throws PassengerHasActiveRideException {
    if (rideRepository.checkActiveRideForPassenger(passengerId)) {
      throw new PassengerHasActiveRideException(passengerId);
    }
  }

  @Override
  public void checkDriverUnavailable(UUID driverId) throws DriverUnavailableException {
    if (!driverRepository.checkDriverAvailable(driverId)) {
      throw new DriverUnavailableException(driverId);
    }
  }

  @Override
  public void checkIdentifier(UUID id, Class<? extends BaseEntity<?>> entityClass) throws IdentifierMissingException {
    if (id == null) {
      throw new IdentifierMissingException(entityClass);
    }
  }

}
