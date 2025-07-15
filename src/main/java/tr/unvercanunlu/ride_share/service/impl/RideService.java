package tr.unvercanunlu.ride_share.service.impl;

import java.math.BigDecimal;
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

  private final IRideDao rideDao;
  private final IDriverDao driverDao;
  private final IPassengerDao passengerDao;
  private final IMapService mapService;

  public RideService(IRideDao rideDao, IDriverDao driverDao, IPassengerDao passengerDao, IMapService mapService) {
    this.rideDao = rideDao;
    this.driverDao = driverDao;
    this.passengerDao = passengerDao;
    this.mapService = mapService;
  }

  @Override
  public RideRequestedDto request(RequestRideDto request) throws PassengerNotFoundException, PassengerHasActiveRideException {
    Passenger passenger = Optional.ofNullable(
        passengerDao.get(request.passengerId())
    ).orElseThrow(() -> new PassengerNotFoundException(request.passengerId()));

    if (rideDao.checkActiveRideForPassenger(passenger.getId())) {
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

  public List<NearRequestedRideDto> findNearestRequestedRides(Location current) {
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
    Ride ride = Optional.ofNullable(
        rideDao.get(request.rideId())
    ).orElseThrow(() -> new RideNotFoundException(request.rideId()));

    Driver driver = Optional.ofNullable(
        driverDao.get(request.driverId())
    ).orElseThrow(() -> new DriverNotFoundException(request.driverId()));

    if (!driver.getStatus().equals(DriverStatus.AVAILABLE)) {
      throw new DriverUnavailableException(driver.getId());
    }

    if (rideDao.checkActiveRideForDriver(driver.getId())) {
      throw new DriverHasActiveRideException(driver.getId());
    }

    ride.setDriverId(driver.getId());

    LocalDateTime acceptedAt = LocalDateTime.now();
    ride.setAcceptedAt(acceptedAt);

    ride.setStatus(RideStatus.ACCEPTED);

    ride = rideDao.save(ride);

    driver.setStatus(DriverStatus.BUSY);

    driverDao.save(driver);

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
    Ride ride = Optional.ofNullable(
        rideDao.get(rideId)
    ).orElseThrow(() -> new RideNotFoundException(rideId));

    if (ride.getDriverId() == null) {
      throw new DriverMissingException(rideId);
    }

    LocalDateTime pickupAt = LocalDateTime.now();
    ride.setPickupAt(pickupAt);

    LocalDateTime pickupEndAt = pickupAt.plusMinutes(AppConfig.MAX_DURATION);
    ride.setPickupEndAt(pickupEndAt);

    ride.setStatus(RideStatus.STARTED);

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
  public RideCompletedDto complete(UUID rideId) throws RideNotFoundException {
    Ride ride = Optional.ofNullable(
        rideDao.get(rideId)
    ).orElseThrow(() -> new RideNotFoundException(rideId));

    if (ride.getDriverId() == null) {
      throw new DriverMissingException(rideId);
    }

    UUID driverId = ride.getDriverId();

    Driver driver = Optional.ofNullable(
        driverDao.get(driverId)
    ).orElseThrow(() -> new DriverNotFoundException(driverId));

    LocalDateTime completedAt = LocalDateTime.now();
    ride.setCompletedAt(completedAt);

    int duration = (int) Duration.between(ride.getPickupAt(), completedAt).toMinutes();
    ride.setDuration(duration);

    ride.setStatus(RideStatus.COMPLETED);

    ride = rideDao.save(ride);

    driver.setStatus(DriverStatus.AVAILABLE);

    driverDao.save(driver);

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
    Ride ride = Optional.ofNullable(
        rideDao.get(rideId)
    ).orElseThrow(() -> new RideNotFoundException(rideId));

    LocalDateTime canceledAt = LocalDateTime.now();

    ride.setCanceledAt(canceledAt);
    ride.setStatus(RideStatus.CANCELED);

    ride = rideDao.save(ride);

    Driver driver = driverDao.get(ride.getDriverId());

    if (driver != null) {
      driver.setStatus(DriverStatus.AVAILABLE);

      driverDao.save(driver);
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
    return Optional.ofNullable(
        rideDao.get(rideId)
    ).orElseThrow(() -> new RideNotFoundException(rideId));
  }

  @Override
  public List<Ride> getHistoryOfPassenger(UUID passengerId) throws PassengerNotFoundException {
    Optional.ofNullable(
        passengerDao.get(passengerId)
    ).orElseThrow(() -> new PassengerNotFoundException(passengerId));

    return rideDao.getByPassenger(passengerId);
  }

  @Override
  public List<Ride> getHistoryOfDriver(UUID driverId) throws DriverNotFoundException {
    Optional.ofNullable(
        driverDao.get(driverId)
    ).orElseThrow(() -> new DriverNotFoundException(driverId));

    return rideDao.getByDriver(driverId);
  }

}
