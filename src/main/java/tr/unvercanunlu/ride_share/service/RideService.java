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
import tr.unvercanunlu.ride_share.exception.DriverMissingException;
import tr.unvercanunlu.ride_share.exception.PassengerNotFoundException;
import tr.unvercanunlu.ride_share.exception.RideNotFoundException;

public interface RideService {

  RideRequestedDto request(RequestRideDto request) throws PassengerNotFoundException;

  List<NearRequestedRideDto> findNearestRequestedRides(Location current);

  RideAcceptedDto accept(AcceptRideDto request) throws DriverMissingException;

  PassengerPickupDto pickupPassenger(UUID rideId) throws DriverMissingException;

  RideCompletedDto complete(UUID rideId) throws DriverMissingException;

  RideCanceledDto cancel(UUID rideId);

  Ride getDetail(UUID rideId) throws RideNotFoundException;

  List<Ride> getHistoryOfPassenger(UUID passengerId);

  List<Ride> getHistoryOfDriver(UUID driverId);

}
