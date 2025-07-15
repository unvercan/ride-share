package tr.unvercanunlu.ride_share.service.impl;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tr.unvercanunlu.ride_share.config.AppConfig;
import tr.unvercanunlu.ride_share.dao.IDriverRepository;
import tr.unvercanunlu.ride_share.dao.IPassengerRepository;
import tr.unvercanunlu.ride_share.dao.IRideRepository;
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
import tr.unvercanunlu.ride_share.exception.DriverHasActiveRideException;
import tr.unvercanunlu.ride_share.exception.DriverMissingException;
import tr.unvercanunlu.ride_share.exception.DriverNotFoundException;
import tr.unvercanunlu.ride_share.exception.DriverUnavailableException;
import tr.unvercanunlu.ride_share.exception.PassengerHasActiveRideException;
import tr.unvercanunlu.ride_share.exception.PassengerNotFoundException;
import tr.unvercanunlu.ride_share.exception.RideNotFoundException;
import tr.unvercanunlu.ride_share.service.IMapService;
import tr.unvercanunlu.ride_share.service.IRideService;
import tr.unvercanunlu.ride_share.status.DriverStatus;
import tr.unvercanunlu.ride_share.status.RideStatus;

public class RideService implements IRideService {

  private final IRideRepository rideRepository;
  private final IDriverRepository driverRepository;
  private final IPassengerRepository passengerRepository;
  private final IMapService mapService;

  public RideService(
      IRideRepository rideRepository,
      IDriverRepository driverRepository,
      IPassengerRepository passengerRepository,
      IMapService mapService
  ) {
    this.rideRepository = rideRepository;
    this.driverRepository = driverRepository;
    this.passengerRepository = passengerRepository;
    this.mapService = mapService;
  }

  @Override
  public RideRequestedDto request(RequestRideDto request) throws PassengerNotFoundException, PassengerHasActiveRideException {
    Passenger passenger = passengerRepository.get(request.passengerId())
        .orElseThrow(() -> new PassengerNotFoundException(request.passengerId()));

    if (rideRepository.checkActiveRideForPassenger(passenger.getId())) {
      throw new PassengerHasActiveRideException(passenger.getId());
    }

    LocalDateTime requestedAt = LocalDateTime.now();
    LocalDateTime requestEndAt = requestedAt.plusMinutes(AppConfig.MAX_DURATION);

    double distance = mapService.calculateDistance(request.pickup(), request.dropOff());

    BigDecimal fare = BigDecimal.valueOf(
        AppConfig.BASE_FARE + (distance * AppConfig.KM_RATE)
    );

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

    ride = rideRepository.save(ride);

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

  public List<NearRequestedRideDto> findNearestRequestedRides(Location current) {
    LocalDateTime gapStart = LocalDateTime.now();
    LocalDateTime gapEnd = gapStart.plusMinutes(AppConfig.MAX_DURATION);

    List<Ride> requestedRides = rideRepository.getRequestedRidesBetweenGap(gapStart, gapEnd);

    List<NearRequestedRideDto> nearRequestedRides = new ArrayList<>();

    for (Ride ride : requestedRides) {
      double distanceToPickup = mapService.calculateDistance(ride.getPickup(), current);

      if (distanceToPickup > AppConfig.NEAR_DISTANCE) {
        continue;
      }

      // estimations
      int estimatedDurationToPickup = -1;
      LocalDateTime estimatedPickupAt = null;
      LocalDateTime estimatedPickupEndAt = null;
      int estimatedDuration = -1;
      LocalDateTime estimatedCompletedAt = null;
      if (AppConfig.ESTIMATION) {
        estimatedDurationToPickup = mapService.estimateDuration(current, ride.getPickup());
        estimatedPickupAt = gapStart.plusMinutes(estimatedDurationToPickup);
        estimatedPickupEndAt = estimatedPickupAt.plusMinutes(AppConfig.MAX_DURATION);
        estimatedDuration = mapService.estimateDuration(ride.getPickup(), ride.getDropOff());
        estimatedCompletedAt = estimatedPickupAt.plusMinutes(estimatedDuration);
      }

      if (ride.getRequestEndAt().isAfter(estimatedPickupAt)) {
        NearRequestedRideDto nearRequestedRide = new NearRequestedRideDto(
            ride.getId(),
            ride.getPassengerId(),
            ride.getPickup(),
            ride.getDropOff(),
            ride.getDistance(),
            ride.getFare(),
            ride.getRequestedAt(),
            ride.getRequestEndAt(),
            current,
            distanceToPickup,

            // estimations
            estimatedPickupAt,
            estimatedPickupEndAt,
            estimatedCompletedAt
        );

        nearRequestedRides.add(nearRequestedRide);
      }
    }

    return nearRequestedRides;
  }

  @Override
  public RideAcceptedDto accept(AcceptRideDto request)
      throws RideNotFoundException, DriverNotFoundException, DriverUnavailableException, DriverHasActiveRideException {
    Ride ride = rideRepository.get(request.rideId())
        .orElseThrow(() -> new RideNotFoundException(request.rideId()));

    checkDriverSuitable(request.driverId());

    ride.setDriverId(request.driverId());

    LocalDateTime acceptedAt = LocalDateTime.now();
    ride.setAcceptedAt(acceptedAt);

    ride.setStatus(RideStatus.ACCEPTED);

    ride = rideRepository.save(ride);

    setDriverBusy(ride.getDriverId());

    // estimations
    int estimatedDurationToPickup = -1;
    LocalDateTime estimatedPickupAt = null;
    LocalDateTime estimatedPickupEndAt = null;
    int estimatedDuration = -1;
    LocalDateTime estimatedCompletedAt = null;
    if (AppConfig.ESTIMATION) {
      estimatedDurationToPickup = mapService.estimateDuration(request.current(), ride.getPickup());
      estimatedPickupAt = acceptedAt.plusMinutes(estimatedDurationToPickup);
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
    Ride ride = rideRepository.get(rideId)
        .orElseThrow(() -> new RideNotFoundException(rideId));

    if (ride.getDriverId() == null) {
      throw new DriverMissingException(rideId);
    }

    LocalDateTime pickupAt = LocalDateTime.now();
    ride.setPickupAt(pickupAt);

    LocalDateTime pickupEndAt = pickupAt.plusMinutes(AppConfig.MAX_DURATION);
    ride.setPickupEndAt(pickupEndAt);

    ride.setStatus(RideStatus.STARTED);

    ride = rideRepository.save(ride);

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
  public RideCompletedDto complete(UUID rideId) throws RideNotFoundException {
    Ride ride = rideRepository.get(rideId)
        .orElseThrow(() -> new RideNotFoundException(rideId));

    if (ride.getDriverId() == null) {
      throw new DriverMissingException(rideId);
    }

    setDriverAvailable(ride.getDriverId());

    LocalDateTime completedAt = LocalDateTime.now();
    ride.setCompletedAt(completedAt);

    int duration = (int) Duration.between(ride.getPickupAt(), completedAt).toMinutes();
    ride.setDuration(duration);

    ride.setStatus(RideStatus.COMPLETED);

    ride = rideRepository.save(ride);

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
  public RideCanceledDto cancel(UUID rideId) throws RideNotFoundException {
    Ride ride = rideRepository.get(rideId)
        .orElseThrow(() -> new RideNotFoundException(rideId));

    LocalDateTime canceledAt = LocalDateTime.now();

    ride.setCanceledAt(canceledAt);
    ride.setStatus(RideStatus.CANCELED);

    ride = rideRepository.save(ride);

    if (ride.getDriverId() != null) {
      setDriverAvailable(ride.getDriverId());
    }

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
  public Ride getDetail(UUID rideId) throws RideNotFoundException {
    return rideRepository.get(rideId)
        .orElseThrow(() -> new RideNotFoundException(rideId));
  }

  @Override
  public List<Ride> getHistoryOfPassenger(UUID passengerId) throws PassengerNotFoundException {
    passengerRepository.get(passengerId)
        .orElseThrow(() -> new PassengerNotFoundException(passengerId));

    return rideRepository.getByPassenger(passengerId);
  }

  @Override
  public List<Ride> getHistoryOfDriver(UUID driverId) throws DriverNotFoundException {
    driverRepository.get(driverId)
        .orElseThrow(() -> new DriverNotFoundException(driverId));

    return rideRepository.getByDriver(driverId);
  }

  private void setDriverAvailable(UUID driverId) throws DriverNotFoundException {
    Driver driver = driverRepository.get(driverId)
        .orElseThrow(() -> new DriverNotFoundException(driverId));

    driver.setStatus(DriverStatus.AVAILABLE);

    driverRepository.save(driver);
  }

  private void setDriverBusy(UUID driverId) throws DriverNotFoundException {
    Driver driver = driverRepository.get(driverId)
        .orElseThrow(() -> new DriverNotFoundException(driverId));

    driver.setStatus(DriverStatus.BUSY);

    driverRepository.save(driver);
  }

  private void checkDriverSuitable(UUID driverId) throws DriverNotFoundException, DriverUnavailableException, DriverHasActiveRideException {
    Driver driver = driverRepository.get(driverId)
        .orElseThrow(() -> new DriverNotFoundException(driverId));

    if (!driver.getStatus().equals(DriverStatus.AVAILABLE)) {
      throw new DriverUnavailableException(driver.getId());
    }

    if (rideRepository.checkActiveRideForDriver(driver.getId())) {
      throw new DriverHasActiveRideException(driver.getId());
    }
  }

}
