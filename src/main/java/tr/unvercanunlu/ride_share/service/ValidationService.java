package tr.unvercanunlu.ride_share.service;

import java.util.UUID;
import tr.unvercanunlu.ride_share.core.entity.BaseEntity;
import tr.unvercanunlu.ride_share.entity.Ride;
import tr.unvercanunlu.ride_share.exception.DriverMissingException;
import tr.unvercanunlu.ride_share.exception.DriverUnavailableException;
import tr.unvercanunlu.ride_share.exception.HasActiveRideException;
import tr.unvercanunlu.ride_share.exception.IdentifierMissingException;
import tr.unvercanunlu.ride_share.exception.NotExpectedRideStatusException;
import tr.unvercanunlu.ride_share.exception.NotFoundException;
import tr.unvercanunlu.ride_share.exception.RideAlreadyAcceptedException;
import tr.unvercanunlu.ride_share.status.RideStatus;

public interface ValidationService {

  void checkNoActiveRideForDriver(UUID driverId) throws HasActiveRideException;

  void checkNoActiveRideForPassenger(UUID passengerId) throws HasActiveRideException;

  void checkDriverAvailable(UUID driverId) throws DriverUnavailableException;

  void checkIdentifier(UUID id, Class<? extends BaseEntity<?>> entityClass) throws IdentifierMissingException;

  void checkPassengerExists(UUID passengerId) throws NotFoundException;

  void checkRideTransition(Ride ride, RideStatus nextStatus) throws NotExpectedRideStatusException;

  void checkDriverPresent(Ride ride) throws DriverMissingException;

  void checkDriverExists(UUID driverId) throws NotFoundException;

  void checkRideAccepted(Ride ride) throws RideAlreadyAcceptedException;

}
