package tr.unvercanunlu.ride_share.service;

import java.util.UUID;
import tr.unvercanunlu.ride_share.exception.DriverHasActiveRideException;
import tr.unvercanunlu.ride_share.exception.DriverNotFoundException;
import tr.unvercanunlu.ride_share.exception.DriverUnavailableException;
import tr.unvercanunlu.ride_share.exception.PassengerHasActiveRideException;

public interface ValidationService {

  void checkActiveRideForDriver(UUID driverId) throws DriverHasActiveRideException;

  void checkActiveRideForPassenger(UUID passengerId) throws PassengerHasActiveRideException;

  void checkDriverUnavailable(UUID driverId) throws DriverNotFoundException, DriverUnavailableException;

}
