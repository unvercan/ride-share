package tr.unvercanunlu.ride_share.service.impl;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
import tr.unvercanunlu.ride_share.helper.LogHelper;
import tr.unvercanunlu.ride_share.service.ValidationService;
import tr.unvercanunlu.ride_share.status.RideStatus;

@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

  private final RideRepository rideRepository;
  private final DriverRepository driverRepository;
  private final PassengerRepository passengerRepository;

  @Override
  public void checkNoActiveRideForDriver(UUID driverId) throws HasActiveRideException {
    try {
      LogHelper.info(this.getClass(),
          String.format("Checking for active rides for driverId=%s", driverId));

      checkIdentifier(driverId, Driver.class);

      if (rideRepository.checkActiveRideExistsForDriver(driverId)) {
        LogHelper.error(this.getClass(),
            String.format("Driver has active ride: driverId=%s", driverId));

        throw new HasActiveRideException(Driver.class, driverId);
      }

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Error during checkNoActiveRideForDriver: driverId=%s error=%s", driverId, e.getMessage()));

      throw e;
    }
  }

  @Override
  public void checkNoActiveRideForPassenger(UUID passengerId) throws HasActiveRideException {
    try {
      LogHelper.info(this.getClass(),
          String.format("Checking for active rides for passengerId=%s", passengerId));

      checkIdentifier(passengerId, Passenger.class);

      if (rideRepository.checkActiveRideExistsForPassenger(passengerId)) {
        LogHelper.error(this.getClass(),
            String.format("Passenger has active ride: passengerId=%s", passengerId));

        throw new HasActiveRideException(Passenger.class, passengerId);
      }

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Error during checkNoActiveRideForPassenger: passengerId=%s error=%s", passengerId, e.getMessage()));

      throw e;
    }
  }

  @Override
  public void checkDriverAvailable(UUID driverId) throws DriverUnavailableException {
    try {
      LogHelper.info(this.getClass(),
          String.format("Checking driver availability: driverId=%s", driverId));

      checkIdentifier(driverId, Driver.class);

      if (!driverRepository.checkAvailable(driverId)) {
        LogHelper.error(this.getClass(),
            String.format("Driver unavailable: driverId=%s", driverId));

        throw new DriverUnavailableException(driverId);
      }

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Error during checkDriverAvailable: driverId=%s error=%s", driverId, e.getMessage()));

      throw e;
    }
  }

  @Override
  public void checkIdentifier(UUID id, Class<? extends BaseEntity<?>> entityClass) throws IdentifierMissingException {
    if (id == null) {
      LogHelper.error(this.getClass(),
          String.format("Identifier is null for entity: %s", entityClass.getSimpleName()));

      throw new IdentifierMissingException(entityClass);
    }
  }

  @Override
  public void checkPassengerExists(UUID passengerId) throws NotFoundException {
    try {
      LogHelper.info(this.getClass(),
          String.format("Checking if passenger exists: passengerId=%s", passengerId));

      checkIdentifier(passengerId, Passenger.class);

      if (!passengerRepository.checkExists(passengerId)) {
        LogHelper.error(this.getClass(),
            String.format("Passenger not found: passengerId=%s", passengerId));

        throw new NotFoundException(Passenger.class, passengerId);
      }

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Error during checkPassengerExists: passengerId=%s error=%s", passengerId, e.getMessage()));

      throw e;
    }
  }

  @Override
  public void checkRideTransition(Ride ride, RideStatus nextStatus) throws NotExpectedRideStatusException {
    try {
      LogHelper.info(this.getClass(),
          String.format("Checking ride transition: rideId=%s currentStatus=%s nextStatus=%s",
              (ride != null ? ride.getId() : null),
              (ride != null && ride.getStatus() != null ? ride.getStatus().name() : null),
              (nextStatus != null ? nextStatus.name() : null)
          ));
      if ((ride != null) && (ride.getStatus() != null) && (nextStatus != null) && !ride.getStatus().canTransitionTo(nextStatus)) {
        LogHelper.error(this.getClass(),
            String.format("Invalid ride status transition: rideId=%s from=%s to=%s", ride.getId(), ride.getStatus(), nextStatus));

        throw new NotExpectedRideStatusException(ride.getId(), ride.getStatus().getAllowedTransitions(), nextStatus);
      }
    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Error during checkRideTransition: rideId=%s error=%s", (ride != null ? ride.getId() : null), e.getMessage()));

      throw e;
    }
  }

  @Override
  public void checkDriverPresent(Ride ride) throws DriverMissingException {
    try {
      LogHelper.info(this.getClass(),
          String.format("Checking driver present on ride: rideId=%s", (ride != null ? ride.getId() : null)));

      if ((ride != null) && (ride.getDriverId() == null)) {
        LogHelper.error(this.getClass(),
            String.format("Driver is missing for ride: rideId=%s", ride.getId()));

        throw new DriverMissingException(ride.getId());
      }

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Error during checkDriverPresent: rideId=%s error=%s", (ride != null ? ride.getId() : null), e.getMessage()));

      throw e;
    }
  }

  @Override
  public void checkDriverExists(UUID driverId) throws NotFoundException {
    try {
      LogHelper.info(this.getClass(),
          String.format("Checking if driver exists: driverId=%s", driverId));

      checkIdentifier(driverId, Driver.class);

      if (!driverRepository.checkExists(driverId)) {
        LogHelper.error(this.getClass(),
            String.format("Driver not found: driverId=%s", driverId));

        throw new NotFoundException(Driver.class, driverId);
      }

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Error during checkDriverExists: driverId=%s error=%s", driverId, e.getMessage()));

      throw e;
    }
  }

  @Override
  public void checkRideAccepted(Ride ride) throws RideAlreadyAcceptedException {
    try {
      LogHelper.info(this.getClass(),
          String.format("Checking if ride already accepted: rideId=%s", (ride != null ? ride.getId() : null)));

      if ((ride != null) && (ride.getDriverId() != null)) {
        LogHelper.error(this.getClass(),
            String.format("Ride already accepted: rideId=%s", ride.getId()));

        throw new RideAlreadyAcceptedException(ride.getId());
      }

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Error during checkRideAccepted: rideId=%s error=%s", (ride != null ? ride.getId() : null), e.getMessage()));

      throw e;
    }
  }

}
