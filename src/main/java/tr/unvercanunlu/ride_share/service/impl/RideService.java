package tr.unvercanunlu.ride_share.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import tr.unvercanunlu.ride_share.config.AppConfig;
import tr.unvercanunlu.ride_share.dao.IDriverDao;
import tr.unvercanunlu.ride_share.dao.IPassengerDao;
import tr.unvercanunlu.ride_share.dao.IRideDao;
import tr.unvercanunlu.ride_share.dao.impl.DriverDao;
import tr.unvercanunlu.ride_share.dao.impl.PassengerDao;
import tr.unvercanunlu.ride_share.dao.impl.RideDao;
import tr.unvercanunlu.ride_share.dto.NearRequestedRideDto;
import tr.unvercanunlu.ride_share.dto.request.AcceptRideDto;
import tr.unvercanunlu.ride_share.dto.request.RequestRideDto;
import tr.unvercanunlu.ride_share.dto.response.PassengerPickupDto;
import tr.unvercanunlu.ride_share.dto.response.RideAcceptedDto;
import tr.unvercanunlu.ride_share.dto.response.RideCanceledDto;
import tr.unvercanunlu.ride_share.dto.response.RideCompletedDto;
import tr.unvercanunlu.ride_share.dto.response.RideRequestedDto;
import tr.unvercanunlu.ride_share.entity.Driver;
import tr.unvercanunlu.ride_share.entity.Location;
import tr.unvercanunlu.ride_share.entity.Passenger;
import tr.unvercanunlu.ride_share.entity.Ride;
import tr.unvercanunlu.ride_share.exception.DriverNotFoundException;
import tr.unvercanunlu.ride_share.exception.PassengerNotFoundException;
import tr.unvercanunlu.ride_share.exception.RideNotFoundException;
import tr.unvercanunlu.ride_share.service.IMapService;
import tr.unvercanunlu.ride_share.service.IRideService;
import tr.unvercanunlu.ride_share.status.RideStatus;

public class RideService implements IRideService {

  private final IRideDao rideDao = new RideDao();
  private final IDriverDao driverDao = new DriverDao();
  private final IPassengerDao passengerDao = new PassengerDao();
  private final IMapService mapService = new MapService();

  @Override
  public RideRequestedDto requestRide(RequestRideDto request) throws PassengerNotFoundException {
    Passenger passenger = Optional.ofNullable(
        passengerDao.get(request.passengerId())
    ).orElseThrow(() -> new PassengerNotFoundException(request.passengerId()));

    LocalDateTime requestedAt = LocalDateTime.now();
    LocalDateTime requestEndAt = requestedAt.plusMinutes(AppConfig.MAX_DURATION);

    double distance = mapService.calculateDistance(request.pickup(), request.dropOff());

    double fare = AppConfig.BASE_FARE + (distance * AppConfig.PER_KM_RATE);

    Ride ride = new Ride();
    ride.setId(UUID.randomUUID());
    ride.setPassengerId(passenger.getId());
    ride.setPickup(request.pickup());
    ride.setDropOff(request.dropOff());
    ride.setDistance(distance);
    ride.setFare(fare);
    ride.setStatus(RideStatus.REQUESTED);
    ride.setRequestedAt(requestedAt);
    ride.setRequestEndAt(requestEndAt);

    ride = rideDao.save(ride);

    // estimations
    int estimatedDuration = -1;
    if (AppConfig.ESTIMATION) {
      estimatedDuration = mapService.estimateDuration(request.pickup(), request.dropOff());
    }

    return new RideRequestedDto(
        ride.getId(),
        ride.getPassengerId(),
        ride.getPickup(),
        ride.getDropOff(),
        ride.getDistance(),
        ride.getFare(),
        ride.getRequestedAt(),
        requestEndAt,

        // estimations
        estimatedDuration
    );
  }

  public List<NearRequestedRideDto> findNearestRequestedRidesBetween(Location current) {
    LocalDateTime gapStart = LocalDateTime.now();
    LocalDateTime gapEnd = gapStart.plusMinutes(AppConfig.MAX_DURATION);

    List<Ride> requestedRides = rideDao.getRequestedRidesBetweenGap(gapStart, gapEnd);

    List<NearRequestedRideDto> nearRequestedRides = new ArrayList<>();

    for (Ride ride : requestedRides) {
      double distanceToPickup = mapService.calculateDistance(ride.getPickup(), current);

      if (distanceToPickup > AppConfig.NEAR_DISTANCE) {
        continue;
      }

      // estimations
      int durationToPickup = -1;
      LocalDateTime estimatedPickedUpStartAt = null;
      LocalDateTime estimatedPickedUpEndAt = null;
      if (AppConfig.ESTIMATION) {
        durationToPickup = mapService.estimateDuration(current, ride.getPickup());
        estimatedPickedUpStartAt = gapStart.plusMinutes(durationToPickup);
        estimatedPickedUpEndAt = estimatedPickedUpStartAt.plusMinutes(AppConfig.MAX_DURATION);
      }

      if (ride.getRequestEndAt().isAfter(estimatedPickedUpStartAt)) {
        NearRequestedRideDto nearRequestedRide = NearRequestedRideDto.builder()
            .id(ride.getId())
            .passengerId(ride.getPassengerId())
            .currentLocation(current)
            .pickUpLocation(ride.getPickup())
            .dropOffLocation(ride.getDropOff())
            .fare(ride.getFare())
            .requestedAt(ride.getRequestedAt())
            .requestEndAt(ride.getRequestEndAt())

            // estimations
            .distanceToPickupLocation(distanceToPickup)
            .estimatedPickedUpStartAt(estimatedPickedUpStartAt)
            .estimatedPickedUpEndAt(estimatedPickedUpEndAt)
            .build();

        nearRequestedRides.add(nearRequestedRide);
      }
    }

    return nearRequestedRides;
  }

  @Override
  public RideAcceptedDto acceptRide(AcceptRideDto request) throws RideNotFoundException, DriverNotFoundException {
    Ride ride = Optional.ofNullable(
        rideDao.get(request.rideId())
    ).orElseThrow(() -> new RideNotFoundException(request.rideId()));

    Driver driver = Optional.ofNullable(
        driverDao.get(request.driverId())
    ).orElseThrow(() -> new DriverNotFoundException(request.driverId()));

    ride.setDriverId(driver.getId());

    LocalDateTime acceptedAt = LocalDateTime.now();
    ride.setAcceptedAt(acceptedAt);

    ride.setStatus(RideStatus.ACCEPTED);

    ride = rideDao.save(ride);

    // estimations
    int durationToPickup = -1;
    LocalDateTime estimatedPickupAt = null;
    LocalDateTime estimatedPickupEndAt = null;
    int estimatedDuration = -1;
    LocalDateTime estimatedCompletedAt = null;
    if (AppConfig.ESTIMATION) {
      durationToPickup = mapService.estimateDuration(request.current(), ride.getPickup());
      estimatedPickupAt = acceptedAt.plusMinutes(durationToPickup);
      estimatedPickupEndAt = estimatedPickupAt.plusMinutes(AppConfig.MAX_DURATION);
      estimatedDuration = mapService.estimateDuration(ride.getPickup(), ride.getDropOff());
      estimatedCompletedAt = estimatedPickupAt.plusMinutes(estimatedDuration);
    }

    return new RideAcceptedDto(
        ride.getId(),
        ride.getPassengerId(),
        ride.getDriverId(),
        ride.getPickup(),
        ride.getDropOff(),
        ride.getDistance(),
        ride.getFare(),
        ride.getRequestedAt(),
        ride.getAcceptedAt(),

        // estimations
        estimatedDuration,
        estimatedPickupAt,
        estimatedPickupEndAt,
        estimatedCompletedAt
    );
  }

  @Override
  public PassengerPickupDto pickupPassenger(UUID rideId) throws RideNotFoundException {
    Ride ride = Optional.ofNullable(
        rideDao.get(rideId)
    ).orElseThrow(() -> new RideNotFoundException(rideId));

    LocalDateTime pickupAt = LocalDateTime.now();
    ride.setPickupAt(pickupAt);

    LocalDateTime pickupEndAt = pickupAt.plusMinutes(AppConfig.MAX_DURATION);
    ride.setPickupEndAt(pickupEndAt);

    ride.setStatus(RideStatus.IN_PROGRESS);

    ride = rideDao.save(ride);

    // estimations
    int estimatedDuration = -1;
    LocalDateTime estimatedCompletedAt = null;
    if (AppConfig.ESTIMATION) {
      estimatedDuration = mapService.estimateDuration(ride.getPickup(), ride.getDropOff());
      estimatedCompletedAt = pickupAt.plusMinutes(estimatedDuration);
    }

    return new PassengerPickupDto(
        ride.getId(),
        ride.getPassengerId(),
        ride.getDriverId(),
        ride.getPickup(),
        ride.getDropOff(),
        ride.getDistance(),
        ride.getFare(),
        ride.getRequestedAt(),
        ride.getAcceptedAt(),
        ride.getPickupAt(),

        // estimations
        estimatedDuration,
        estimatedCompletedAt
    );
  }

  @Override
  public RideCompletedDto completeRide(UUID rideId) throws RideNotFoundException {
    Ride ride = Optional.ofNullable(
        rideDao.get(rideId)
    ).orElseThrow(() -> new RideNotFoundException(rideId));

    LocalDateTime completedAt = LocalDateTime.now();
    ride.setCompletedAt(completedAt);

    int duration = (int) Duration.between(ride.getPickupAt(), completedAt).toMinutes();
    ride.setDuration(duration);

    ride.setStatus(RideStatus.COMPLETED);

    ride = rideDao.save(ride);

    return new RideCompletedDto(
        ride.getId(),
        ride.getPassengerId(),
        ride.getDriverId(),
        ride.getPickup(),
        ride.getDropOff(),
        ride.getDistance(),
        ride.getFare(),
        ride.getRequestedAt(),
        ride.getAcceptedAt(),
        ride.getPickupAt(),
        ride.getCompletedAt(),
        duration
    );
  }

  @Override
  public RideCanceledDto cancelRide(UUID rideId) throws RideNotFoundException {
    Ride ride = Optional.ofNullable(
        rideDao.get(rideId)
    ).orElseThrow(() -> new RideNotFoundException(rideId));

    LocalDateTime canceledAt = LocalDateTime.now();

    ride.setCanceledAt(canceledAt);
    ride.setStatus(RideStatus.CANCELED);

    ride = rideDao.save(ride);

    return new RideCanceledDto(
        ride.getId(),
        ride.getPassengerId(),
        ride.getDriverId(),
        ride.getPickup(),
        ride.getDropOff(),
        ride.getDistance(),
        ride.getFare(),
        ride.getRequestedAt(),
        ride.getCanceledAt()
    );
  }

  @Override
  public Ride getRideDetail(UUID rideId) throws RideNotFoundException {
    return Optional.ofNullable(
        rideDao.get(rideId)
    ).orElseThrow(() -> new RideNotFoundException(rideId));
  }

  @Override
  public List<Ride> getRideHistoryOfPassenger(UUID passengerId) throws PassengerNotFoundException {
    Optional.ofNullable(
        passengerDao.get(passengerId)
    ).orElseThrow(() -> new PassengerNotFoundException(passengerId));

    return rideDao.getByPassenger(passengerId);
  }

  @Override
  public List<Ride> getRideHistoryOfDriver(UUID driverId) throws DriverNotFoundException {
    Optional.ofNullable(
        driverDao.get(driverId)
    ).orElseThrow(() -> new DriverNotFoundException(driverId));

    return rideDao.getByDriver(driverId);
  }

}
