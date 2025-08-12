package tr.unvercanunlu.ride_share.service.impl;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import tr.unvercanunlu.ride_share.core.entity.BaseEntity;
import tr.unvercanunlu.ride_share.core.log.Logger;
import tr.unvercanunlu.ride_share.dao.DriverRepository;
import tr.unvercanunlu.ride_share.dao.PassengerRepository;
import tr.unvercanunlu.ride_share.dao.RideRepository;
import tr.unvercanunlu.ride_share.entity.Driver;
import tr.unvercanunlu.ride_share.entity.Passenger;
import tr.unvercanunlu.ride_share.entity.Ride;
import tr.unvercanunlu.ride_share.exception.DriverMissingException;
import tr.unvercanunlu.ride_share.exception.DriverUnavailableException;
import tr.unvercanunlu.ride_share.exception.HasActiveRideException;
import tr.unvercanunlu.ride_share.exception.IdentifierMissingException;
import tr.unvercanunlu.ride_share.exception.NotExpectedRideStatusException;
import tr.unvercanunlu.ride_share.exception.NotFoundException;
import tr.unvercanunlu.ride_share.exception.RideAlreadyAcceptedException;
import tr.unvercanunlu.ride_share.helper.LoggerFactory;
import tr.unvercanunlu.ride_share.service.ValidationService;
import tr.unvercanunlu.ride_share.status.RideStatus;

@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

  private static final Logger logger = LoggerFactory.getLogger(ValidationServiceImpl.class);

  private final RideRepository rideRepository;
  private final DriverRepository driverRepository;
  private final PassengerRepository passengerRepository;

  @Override
  public void checkNoActiveRideForDriver(UUID driverId) throws HasActiveRideException {
    checkIdentifier(driverId, Driver.class);

    if (rideRepository.existsActiveByDriverId(driverId)) {
      logger.error(String.format("Driver has active ride: driverId=%s", driverId));
      throw new HasActiveRideException(Driver.class, driverId);
    }
  }

  @Override
  public void checkNoActiveRideForPassenger(UUID passengerId) throws HasActiveRideException {
    checkIdentifier(passengerId, Passenger.class);

    if (rideRepository.existsActiveByPassengerId(passengerId)) {
      logger.error(String.format("Passenger has active ride: passengerId=%s", passengerId));
      throw new HasActiveRideException(Passenger.class, passengerId);
    }
  }

  @Override
  public void checkDriverAvailable(UUID driverId) throws DriverUnavailableException {
    checkIdentifier(driverId, Driver.class);

    if (!driverRepository.isAvailable(driverId)) {
      logger.error(String.format("Driver unavailable: driverId=%s", driverId));
      throw new DriverUnavailableException(driverId);
    }
  }

  @Override
  public void checkIdentifier(UUID id, Class<? extends BaseEntity<?>> entityClass) throws IdentifierMissingException {
    if (id == null) {
      logger.error(String.format("Identifier is null for entity: %s", entityClass.getSimpleName()));
      throw new IdentifierMissingException(entityClass);
    }
  }

  @Override
  public void checkPassengerExists(UUID passengerId) throws NotFoundException {
    checkIdentifier(passengerId, Passenger.class);

    if (!passengerRepository.existsById(passengerId)) {
      logger.error(String.format("Passenger not found: passengerId=%s", passengerId));
      throw new NotFoundException(Passenger.class, passengerId);
    }
  }

  @Override
  public void checkRideTransition(Ride ride, RideStatus nextStatus) throws NotExpectedRideStatusException {
    if ((ride != null) && (ride.getStatus() != null) && (nextStatus != null) && !ride.getStatus().canTransitionTo(nextStatus)) {
      logger.error(String.format("Invalid ride status transition: rideId=%s from=%s to=%s", ride.getId(), ride.getStatus(), nextStatus));
      throw new NotExpectedRideStatusException(ride.getId(), ride.getStatus().getAllowedTransitions(), nextStatus);
    }
  }

  @Override
  public void checkDriverPresent(Ride ride) throws DriverMissingException {
    if ((ride != null) && (ride.getDriverId() == null)) {
      logger.error(String.format("Driver is missing for ride: rideId=%s", ride.getId()));
      throw new DriverMissingException(ride.getId());
    }
  }

  @Override
  public void checkDriverExists(UUID driverId) throws NotFoundException {
    checkIdentifier(driverId, Driver.class);

    if (!driverRepository.existsById(driverId)) {
      logger.error(String.format("Driver not found: driverId=%s", driverId));
      throw new NotFoundException(Driver.class, driverId);
    }
  }

  @Override
  public void checkRideAccepted(Ride ride) throws RideAlreadyAcceptedException {
    if ((ride != null) && (ride.getDriverId() != null)) {
      logger.error(String.format("Ride already accepted: rideId=%s", ride.getId()));
      throw new RideAlreadyAcceptedException(ride.getId());
    }
  }

}
