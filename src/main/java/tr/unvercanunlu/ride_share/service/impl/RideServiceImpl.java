package tr.unvercanunlu.ride_share.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tr.unvercanunlu.ride_share.config.AppConfig;
import tr.unvercanunlu.ride_share.dao.DriverRepository;
import tr.unvercanunlu.ride_share.dao.PassengerRepository;
import tr.unvercanunlu.ride_share.dao.RideRepository;
import tr.unvercanunlu.ride_share.dto.NearRequestedRideDto;
import tr.unvercanunlu.ride_share.dto.request.AcceptRideDto;
import tr.unvercanunlu.ride_share.dto.request.RequestRideDto;
import tr.unvercanunlu.ride_share.dto.response.Estimation;
import tr.unvercanunlu.ride_share.dto.response.PassengerPickupDto;
import tr.unvercanunlu.ride_share.dto.response.RideAcceptedDto;
import tr.unvercanunlu.ride_share.dto.response.RideCanceledDto;
import tr.unvercanunlu.ride_share.dto.response.RideCompletedDto;
import tr.unvercanunlu.ride_share.dto.response.RideRequestedDto;
import tr.unvercanunlu.ride_share.entity.Driver;
import tr.unvercanunlu.ride_share.entity.Location;
import tr.unvercanunlu.ride_share.entity.Passenger;
import tr.unvercanunlu.ride_share.entity.Ride;
import tr.unvercanunlu.ride_share.exception.DriverMissingForRideException;
import tr.unvercanunlu.ride_share.exception.DriverNotFoundException;
import tr.unvercanunlu.ride_share.exception.IdentifierMissingException;
import tr.unvercanunlu.ride_share.exception.PassengerNotFoundException;
import tr.unvercanunlu.ride_share.exception.RideNotFoundException;
import tr.unvercanunlu.ride_share.service.CalculationService;
import tr.unvercanunlu.ride_share.service.EstimationService;
import tr.unvercanunlu.ride_share.service.GeoService;
import tr.unvercanunlu.ride_share.service.RideService;
import tr.unvercanunlu.ride_share.service.ValidationService;
import tr.unvercanunlu.ride_share.status.DriverStatus;
import tr.unvercanunlu.ride_share.status.RideStatus;

public class RideServiceImpl implements RideService {

  private final RideRepository rideRepository;
  private final DriverRepository driverRepository;
  private final PassengerRepository passengerRepository;
  private final GeoService geoService;
  private final CalculationService calculationService;
  private final EstimationService estimationService;
  private final ValidationService validationService;

  public RideServiceImpl(
      RideRepository rideRepository,
      DriverRepository driverRepository,
      PassengerRepository passengerRepository,
      GeoService geoService,
      CalculationService calculationService,
      EstimationService estimationService,
      ValidationService validationService
  ) {
    this.rideRepository = rideRepository;
    this.driverRepository = driverRepository;
    this.passengerRepository = passengerRepository;
    this.geoService = geoService;
    this.calculationService = calculationService;
    this.estimationService = estimationService;
    this.validationService = validationService;
  }

  @Override
  public RideRequestedDto request(RequestRideDto request) throws IdentifierMissingException, PassengerNotFoundException {
    if (request.passengerId() == null) {
      throw new IdentifierMissingException(Passenger.class);
    }

    passengerRepository.get(request.passengerId()).orElseThrow(() -> new PassengerNotFoundException(request.passengerId()));
    validationService.checkActiveRideForPassenger(request.passengerId());
    Ride ride = Ride.of(request);
    ride.setRequestedAt(LocalDateTime.now());
    ride.setRequestEndAt(ride.getRequestedAt().plusMinutes(AppConfig.MAX_DURATION));
    ride.setDistance(geoService.calculateDistance(request.pickup(), request.dropOff()));
    ride.setFare(calculationService.calculatePrice(ride.getDistance()));
    ride = rideRepository.save(ride);

    Estimation estimation = null;
    if (AppConfig.ESTIMATION) {
      estimation = estimationService.estimate(request.pickup(), request.dropOff());
    }

    return new RideRequestedDto(
        ride.getId(), ride.getPassengerId(), ride.getPickup(), ride.getDropOff(), ride.getDistance(),
        ride.getFare(), ride.getRequestedAt(), ride.getRequestedAt(), estimation
    );
  }

  @Override
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

      Estimation estimation = null;
      if (AppConfig.ESTIMATION) {
        estimation = estimationService.estimate(ride.getPickup(), ride.getDropOff(), current, gapStart);

        if (ride.getRequestEndAt().isAfter(estimation.pickupAt())) {
          continue;
        }
      }

      NearRequestedRideDto nearRequestedRide = new NearRequestedRideDto(
          ride.getId(), ride.getPassengerId(), ride.getPickup(), ride.getDropOff(), ride.getDistance(),
          ride.getFare(), ride.getRequestedAt(), ride.getRequestEndAt(), current, distanceToPickup, estimation
      );

      nearRequestedRides.add(nearRequestedRide);
    }

    return nearRequestedRides;
  }

  @Override
  public RideAcceptedDto accept(AcceptRideDto request) {
    Ride ride = getDetail(request.rideId());

    if (ride.getDriverId() == null) {
      throw new DriverMissingForRideException(ride.getId());
    }

    validationService.checkDriverUnavailable(request.driverId());
    validationService.checkActiveRideForDriver(request.driverId());
    setDriverBusy(ride.getDriverId());
    ride.setDriverId(request.driverId());
    ride.setAcceptedAt(LocalDateTime.now());
    ride.setStatus(RideStatus.ACCEPTED);
    ride = rideRepository.save(ride);

    Estimation estimation = null;
    if (AppConfig.ESTIMATION) {
      estimation = estimationService.estimate(ride.getPickup(), ride.getDropOff(), request.current(), ride.getAcceptedAt());
    }

    return new RideAcceptedDto(
        ride.getId(), ride.getPassengerId(), ride.getDriverId(), ride.getPickup(), ride.getDropOff(),
        ride.getDistance(), ride.getFare(), ride.getRequestedAt(), ride.getAcceptedAt(), estimation
    );
  }

  @Override
  public PassengerPickupDto pickupPassenger(UUID rideId) throws DriverMissingForRideException {
    Ride ride = getDetail(rideId);

    if (ride.getDriverId() == null) {
      throw new DriverMissingForRideException(rideId);
    }

    setDriverBusy(ride.getDriverId());
    ride.setPickupAt(LocalDateTime.now());
    ride.setPickupEndAt(ride.getPickupAt().plusMinutes(AppConfig.MAX_DURATION));
    ride.setStatus(RideStatus.STARTED);
    ride = rideRepository.save(ride);

    Estimation estimation = null;
    if (AppConfig.ESTIMATION) {
      estimation = estimationService.estimate(ride.getPickup(), ride.getDropOff(), ride.getPickupAt());
    }

    return new PassengerPickupDto(
        ride.getId(), ride.getPassengerId(), ride.getDriverId(), ride.getPickup(), ride.getDropOff(), ride.getDistance(),
        ride.getFare(), ride.getRequestedAt(), ride.getAcceptedAt(), ride.getPickupAt(), estimation
    );
  }

  @Override
  public RideCompletedDto complete(UUID rideId) throws DriverMissingForRideException {
    Ride ride = getDetail(rideId);

    if (ride.getDriverId() == null) {
      throw new DriverMissingForRideException(rideId);
    }

    setDriverAvailable(ride.getDriverId());
    ride.setCompletedAt(LocalDateTime.now());
    ride.setDuration(Duration.between(ride.getPickupAt(), ride.getCompletedAt()).toMinutes());
    ride.setStatus(RideStatus.COMPLETED);
    ride = rideRepository.save(ride);

    return new RideCompletedDto(
        ride.getId(), ride.getPassengerId(), ride.getDriverId(), ride.getPickup(), ride.getDropOff(), ride.getDistance(),
        ride.getFare(), ride.getRequestedAt(), ride.getAcceptedAt(), ride.getPickupAt(), ride.getCompletedAt(), ride.getDuration()
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
        ride.getId(), ride.getPassengerId(), ride.getDriverId(), ride.getPickup(), ride.getDropOff(),
        ride.getDistance(), ride.getFare(), ride.getRequestedAt(), ride.getCanceledAt()
    );
  }

  @Override
  public Ride getDetail(UUID rideId) throws IdentifierMissingException, RideNotFoundException {
    if (rideId == null) {
      throw new IdentifierMissingException(Ride.class);
    }

    return rideRepository.get(rideId).orElseThrow(() -> new RideNotFoundException(rideId));
  }

  @Override
  public List<Ride> getHistoryOfPassenger(UUID passengerId) throws IdentifierMissingException {
    if (passengerId == null) {
      throw new IdentifierMissingException(Passenger.class);
    }

    return rideRepository.getByPassenger(passengerId);
  }

  @Override
  public List<Ride> getHistoryOfDriver(UUID driverId) throws IdentifierMissingException {
    if (driverId == null) {
      throw new IdentifierMissingException(Driver.class);
    }

    return rideRepository.getByDriver(driverId);
  }

  private void setDriverAvailable(UUID driverId) throws DriverNotFoundException {
    Driver driver = driverRepository.get(driverId).orElseThrow(() -> new DriverNotFoundException(driverId));
    driver.setStatus(DriverStatus.AVAILABLE);
    driverRepository.save(driver);
  }

  private void setDriverBusy(UUID driverId) throws DriverNotFoundException {
    Driver driver = driverRepository.get(driverId).orElseThrow(() -> new DriverNotFoundException(driverId));
    driver.setStatus(DriverStatus.BUSY);
    driverRepository.save(driver);
  }

}
