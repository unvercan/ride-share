package tr.unvercanunlu.ride_share.service.impl;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tr.unvercanunlu.ride_share.config.AppConfig;
import tr.unvercanunlu.ride_share.dao.DriverRepository;
import tr.unvercanunlu.ride_share.dao.RideRepository;
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
import tr.unvercanunlu.ride_share.exception.DriverUnavailableException;
import tr.unvercanunlu.ride_share.exception.PassengerHasActiveRideException;
import tr.unvercanunlu.ride_share.exception.RideNotFoundException;
import tr.unvercanunlu.ride_share.service.CalculationService;
import tr.unvercanunlu.ride_share.service.DriverService;
import tr.unvercanunlu.ride_share.service.GeoService;
import tr.unvercanunlu.ride_share.service.PassengerService;
import tr.unvercanunlu.ride_share.service.RideService;
import tr.unvercanunlu.ride_share.status.DriverStatus;
import tr.unvercanunlu.ride_share.status.RideStatus;

public class RideServiceImpl implements RideService {

  private final RideRepository rideRepository;
  private final DriverRepository driverRepository;
  private final DriverService driverService;
  private final PassengerService passengerService;
  private final GeoService geoService;
  private final CalculationService calculationService;

  public RideServiceImpl(
      RideRepository rideRepository,
      DriverRepository driverRepository,
      DriverService driverService,
      PassengerService passengerService,
      GeoService geoService,
      CalculationService calculationService
  ) {
    this.rideRepository = rideRepository;
    this.driverRepository = driverRepository;
    this.driverService = driverService;
    this.passengerService = passengerService;
    this.geoService = geoService;
    this.calculationService = calculationService;
  }

  @Override
  public RideRequestedDto request(RequestRideDto request) throws PassengerHasActiveRideException {
    Passenger passenger = passengerService.getDetail(request.passengerId());

    if (rideRepository.checkActiveRideForPassenger(passenger.getId())) {
      throw new PassengerHasActiveRideException(passenger.getId());
    }

    LocalDateTime requestedAt = LocalDateTime.now();
    LocalDateTime requestEndAt = requestedAt.plusMinutes(AppConfig.MAX_DURATION);

    double distance = geoService.calculateDistance(request.pickup(), request.dropOff());

    BigDecimal fare = calculationService.calculatePrice(distance);

    Ride ride = new Ride();
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
      estimatedDuration = geoService.estimateDuration(request.pickup(), request.dropOff());
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
      double distanceToPickup = geoService.calculateDistance(ride.getPickup(), current);

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
        estimatedDurationToPickup = geoService.estimateDuration(current, ride.getPickup());
        estimatedPickupAt = gapStart.plusMinutes(estimatedDurationToPickup);
        estimatedPickupEndAt = estimatedPickupAt.plusMinutes(AppConfig.MAX_DURATION);
        estimatedDuration = geoService.estimateDuration(ride.getPickup(), ride.getDropOff());
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
  public RideAcceptedDto accept(AcceptRideDto request) {
    Ride ride = getDetail(request.rideId());

    checkDriverSuitable(request.driverId());

    ride.setDriverId(request.driverId());
    ride.setAcceptedAt(LocalDateTime.now());
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
      estimatedDurationToPickup = geoService.estimateDuration(request.current(), ride.getPickup());
      estimatedPickupAt = ride.getAcceptedAt().plusMinutes(estimatedDurationToPickup);
      estimatedPickupEndAt = estimatedPickupAt.plusMinutes(AppConfig.MAX_DURATION);
      estimatedDuration = geoService.estimateDuration(ride.getPickup(), ride.getDropOff());
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
  public PassengerPickupDto pickupPassenger(UUID rideId) throws DriverMissingException {
    Ride ride = getDetail(rideId);

    if (ride.getDriverId() == null) {
      throw new DriverMissingException(rideId);
    }

    ride.setPickupAt(LocalDateTime.now());
    ride.setPickupEndAt(ride.getPickupAt().plusMinutes(AppConfig.MAX_DURATION));
    ride.setStatus(RideStatus.STARTED);

    ride = rideRepository.save(ride);

    // estimations
    int estimatedDuration = -1;
    LocalDateTime estimatedCompletedAt = null;
    if (AppConfig.ESTIMATION) {
      estimatedDuration = geoService.estimateDuration(ride.getPickup(), ride.getDropOff());
      estimatedCompletedAt = ride.getPickupAt().plusMinutes(estimatedDuration);
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
  public RideCompletedDto complete(UUID rideId) throws DriverMissingException {
    Ride ride = getDetail(rideId);

    if (ride.getDriverId() == null) {
      throw new DriverMissingException(rideId);
    }

    setDriverAvailable(ride.getDriverId());

    ride.setCompletedAt(LocalDateTime.now());
    ride.setDuration(Duration.between(ride.getPickupAt(), ride.getCompletedAt()).toMinutes());
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
        ride.getDuration()
    );
  }

  @Override
  public RideCanceledDto cancel(UUID rideId) {
    Ride ride = getDetail(rideId);

    ride.setCanceledAt(LocalDateTime.now());
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
  public List<Ride> getHistoryOfPassenger(UUID passengerId) {
    passengerService.getDetail(passengerId);

    return rideRepository.getByPassenger(passengerId);
  }

  @Override
  public List<Ride> getHistoryOfDriver(UUID driverId) {
    driverService.getDetail(driverId);

    return rideRepository.getByDriver(driverId);
  }

  private void setDriverAvailable(UUID driverId) {
    Driver driver = driverService.getDetail(driverId);

    driver.setStatus(DriverStatus.AVAILABLE);

    driverRepository.save(driver);
  }

  private void setDriverBusy(UUID driverId) {
    Driver driver = driverService.getDetail(driverId);

    driver.setStatus(DriverStatus.BUSY);

    driverRepository.save(driver);
  }

  private void checkDriverSuitable(UUID driverId) throws DriverUnavailableException, DriverHasActiveRideException {
    Driver driver = driverService.getDetail(driverId);

    if (!driver.getStatus().equals(DriverStatus.AVAILABLE)) {
      throw new DriverUnavailableException(driver.getId());
    }

    if (rideRepository.checkActiveRideForDriver(driver.getId())) {
      throw new DriverHasActiveRideException(driver.getId());
    }
  }

}
