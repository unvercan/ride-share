package tr.unvercanunlu.ride_share.service;

import java.util.List;
import java.util.UUID;
import tr.unvercanunlu.ride_share.dto.request.AcceptRideDto;
import tr.unvercanunlu.ride_share.dto.request.RequestRideDto;
import tr.unvercanunlu.ride_share.dto.response.DriverApprovedDto;
import tr.unvercanunlu.ride_share.dto.response.NearRequestedRideDto;
import tr.unvercanunlu.ride_share.dto.response.PassengerPickupDto;
import tr.unvercanunlu.ride_share.dto.response.RideAcceptedDto;
import tr.unvercanunlu.ride_share.dto.response.RideCanceledDto;
import tr.unvercanunlu.ride_share.dto.response.RideCompletedDto;
import tr.unvercanunlu.ride_share.dto.response.RideRequestedDto;
import tr.unvercanunlu.ride_share.entity.Location;
import tr.unvercanunlu.ride_share.entity.Ride;
import tr.unvercanunlu.ride_share.exception.NotFoundException;

public interface RideService {

  RideRequestedDto request(RequestRideDto request);

  List<NearRequestedRideDto> findNearestRequestedRides(Location current);

  RideAcceptedDto accept(AcceptRideDto request);

  DriverApprovedDto approveDriver(UUID rideId);

  RideRequestedDto disapproveDriver(UUID rideId);

  PassengerPickupDto pickupPassenger(UUID rideId);

  RideCompletedDto complete(UUID rideId);

  RideCanceledDto cancel(UUID rideId);

  Ride getDetail(UUID rideId) throws NotFoundException;

  List<Ride> getHistoryOfPassenger(UUID passengerId);

  List<Ride> getHistoryOfDriver(UUID driverId);

}
