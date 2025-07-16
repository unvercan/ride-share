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
import tr.unvercanunlu.ride_share.exception.DriverMissingForRideException;
import tr.unvercanunlu.ride_share.exception.PassengerNotFoundException;
import tr.unvercanunlu.ride_share.exception.RideNotFoundException;

public interface RideService {

  RideRequestedDto request(RequestRideDto request) throws PassengerNotFoundException;

  List<NearRequestedRideDto> findNearestRequestedRides(Location current);

  RideAcceptedDto accept(AcceptRideDto request) throws DriverMissingForRideException;

  PassengerPickupDto pickupPassenger(UUID rideId) throws DriverMissingForRideException;

  RideCompletedDto complete(UUID rideId) throws DriverMissingForRideException;

  RideCanceledDto cancel(UUID rideId);

  Ride getDetail(UUID rideId) throws RideNotFoundException;

  List<Ride> getHistoryOfPassenger(UUID passengerId);

  List<Ride> getHistoryOfDriver(UUID driverId);

}
