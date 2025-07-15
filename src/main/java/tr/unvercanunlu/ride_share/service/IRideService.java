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
import tr.unvercanunlu.ride_share.exception.DriverNotFoundException;
import tr.unvercanunlu.ride_share.exception.PassengerNotFoundException;
import tr.unvercanunlu.ride_share.exception.RideNotFoundException;

public interface IRideService {

  RideRequestedDto requestRide(RequestRideDto request) throws PassengerNotFoundException;

  List<NearRequestedRideDto> findNearestRequestedRidesBetween(Location current);

  RideAcceptedDto acceptRide(AcceptRideDto request) throws RideNotFoundException, DriverNotFoundException;

  PassengerPickupDto pickupPassenger(UUID rideId) throws RideNotFoundException;

  RideCompletedDto completeRide(UUID rideId) throws RideNotFoundException;

  RideCanceledDto cancelRide(UUID rideId) throws RideNotFoundException;

  Ride getRideDetail(UUID rideId) throws RideNotFoundException;

  List<Ride> getRideHistoryOfPassenger(UUID passengerId) throws PassengerNotFoundException;

  List<Ride> getRideHistoryOfDriver(UUID driverId) throws DriverNotFoundException;

}
