package tr.unvercanunlu.ride_share.service.impl;

import java.util.UUID;
import tr.unvercanunlu.ride_share.core.BaseEntity;
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
import tr.unvercanunlu.ride_share.exception.RideAlreadyCompletedException;
import tr.unvercanunlu.ride_share.service.ValidationService;
import tr.unvercanunlu.ride_share.status.RideStatus;

public class ValidationServiceImpl implements ValidationService {

  private final RideRepository rideRepository;
  private final DriverRepository driverRepository;
  private final PassengerRepository passengerRepository;

  public ValidationServiceImpl(
      RideRepository rideRepository,
      DriverRepository driverRepository,
      PassengerRepository passengerRepository
  ) {
    this.rideRepository = rideRepository;
    this.driverRepository = driverRepository;
    this.passengerRepository = passengerRepository;
  }

  @Override
  public void checkNoActiveRideForDriver(UUID driverId) throws HasActiveRideException {
    checkIdentifier(driverId, Driver.class);

    if (rideRepository.checkActiveRideExistsForDriver(driverId)) {
      throw new HasActiveRideException(Driver.class, driverId);
    }
  }

  @Override
  public void checkNoActiveRideForPassenger(UUID passengerId) throws HasActiveRideException {
    checkIdentifier(passengerId, Passenger.class);

    if (rideRepository.checkActiveRideExistsForPassenger(passengerId)) {
      throw new HasActiveRideException(Passenger.class, passengerId);
    }
  }

  @Override
  public void checkDriverAvailable(UUID driverId) throws DriverUnavailableException {
    checkIdentifier(driverId, Driver.class);

    if (!driverRepository.checkAvailable(driverId)) {
      throw new DriverUnavailableException(driverId);
    }
  }

  @Override
  public void checkIdentifier(UUID id, Class<? extends BaseEntity<?>> entityClass) throws IdentifierMissingException {
    if (id == null) {
      throw new IdentifierMissingException(entityClass);
    }
  }

  @Override
  public void checkPassengerExists(UUID passengerId) throws NotFoundException {
    checkIdentifier(passengerId, Passenger.class);

    if (!passengerRepository.checkExists(passengerId)) {
      throw new NotFoundException(Passenger.class, passengerId);
    }
  }

  // todo
  @Override
  public void checkRideTransition(Ride ride, RideStatus nextStatus) throws NotExpectedRideStatusException {
    if ((ride != null) && (ride.getStatus() != null) && (nextStatus != null) && !ride.getStatus().canTransitionTo(nextStatus)) {
      throw new NotExpectedRideStatusException(ride.getId(), ride.getStatus().getAllowedTransitions(), nextStatus);
    }
  }

  // todo
  @Override
  public void checkDriverPresent(Ride ride) throws DriverMissingException {
    if ((ride != null) && (ride.getDriverId() == null)) {
      throw new DriverMissingException(ride.getId());
    }
  }

  @Override
  public void checkDriverExists(UUID driverId) throws NotFoundException {
    checkIdentifier(driverId, Driver.class);

    if (!driverRepository.checkExists(driverId)) {
      throw new NotFoundException(Driver.class, driverId);
    }
  }

  // todo
  @Override
  public void checkRideCompleted(Ride ride) throws RideAlreadyCompletedException {
    if ((ride != null) && (ride.getStatus() != null) && RideStatus.COMPLETED.equals(ride.getStatus())) {
      throw new RideAlreadyCompletedException(ride.getId());
    }
  }

  // todo
  @Override
  public void checkRideAccepted(Ride ride) throws RideAlreadyAcceptedException {
    if ((ride != null) && (ride.getDriverId() != null)) {
      throw new RideAlreadyAcceptedException(ride.getId());
    }
  }

}
