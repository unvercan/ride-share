package tr.unvercanunlu.ride_share.service;

import java.util.List;
import java.util.UUID;
import tr.unvercanunlu.ride_share.dto.NearRequestedRideDto;
import tr.unvercanunlu.ride_share.dto.request.AcceptRideDto;
import tr.unvercanunlu.ride_share.dto.request.RequestRideDto;
import tr.unvercanunlu.ride_share.dto.response.PassengerPickupDto;
import tr.unvercanunlu.ride_share.dto.response.RideAcceptedDto;
import tr.unvercanunlu.ride_share.dto.response.RideCanceledDto;
import tr.unvercanunlu.ride_share.dto.response.RideCompletedDto;
import tr.unvercanunlu.ride_share.dto.response.RideRequestedDto;
import tr.unvercanunlu.ride_share.entity.Location;
import tr.unvercanunlu.ride_share.entity.Ride;
import tr.unvercanunlu.ride_share.exception.DriverHasActiveRideException;
import tr.unvercanunlu.ride_share.exception.DriverNotFoundException;
import tr.unvercanunlu.ride_share.exception.DriverUnavailableException;
import tr.unvercanunlu.ride_share.exception.PassengerHasActiveRideException;
import tr.unvercanunlu.ride_share.exception.PassengerNotFoundException;
import tr.unvercanunlu.ride_share.exception.RideNotFoundException;

public interface IRideService {

  RideRequestedDto request(RequestRideDto request)
      throws PassengerNotFoundException, PassengerHasActiveRideException;

  List<NearRequestedRideDto> findNearestRequestedRides(Location current);

  RideAcceptedDto accept(AcceptRideDto request)
      throws RideNotFoundException, DriverNotFoundException, DriverUnavailableException, DriverHasActiveRideException;

  PassengerPickupDto pickupPassenger(UUID rideId) throws RideNotFoundException;

  RideCompletedDto complete(UUID rideId) throws RideNotFoundException;

  RideCanceledDto cancel(UUID rideId) throws RideNotFoundException;

  Ride getDetail(UUID rideId) throws RideNotFoundException;

  List<Ride> getHistoryOfPassenger(UUID passengerId) throws PassengerNotFoundException;

  List<Ride> getHistoryOfDriver(UUID driverId) throws DriverNotFoundException;

}
