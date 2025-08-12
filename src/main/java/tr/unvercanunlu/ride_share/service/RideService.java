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

  RideRequestedDto requestRide(RequestRideDto request);

  List<NearRequestedRideDto> findNearestRequestedRides(Location current);

  RideAcceptedDto acceptRide(AcceptRideDto request);

  DriverApprovedDto approveAssignedDriver(UUID rideId);

  RideRequestedDto rejectAssignedDriver(UUID rideId);

  PassengerPickupDto startTrip(UUID rideId);

  RideCompletedDto completeTrip(UUID rideId);

  RideCanceledDto cancelRide(UUID rideId);

  Ride getRide(UUID rideId) throws NotFoundException;

  List<Ride> getHistoryOfPassenger(UUID passengerId);

  List<Ride> getHistoryOfDriver(UUID driverId);

}
