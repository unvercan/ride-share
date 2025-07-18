package tr.unvercanunlu.ride_share.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import tr.unvercanunlu.ride_share.config.AppConfig;
import tr.unvercanunlu.ride_share.dao.DriverRepository;
import tr.unvercanunlu.ride_share.dao.RideRepository;
import tr.unvercanunlu.ride_share.dto.request.AcceptRideDto;
import tr.unvercanunlu.ride_share.dto.request.RequestRideDto;
import tr.unvercanunlu.ride_share.dto.response.DriverApprovedDto;
import tr.unvercanunlu.ride_share.dto.response.Estimation;
import tr.unvercanunlu.ride_share.dto.response.NearRequestedRideDto;
import tr.unvercanunlu.ride_share.dto.response.PassengerPickupDto;
import tr.unvercanunlu.ride_share.dto.response.RideAcceptedDto;
import tr.unvercanunlu.ride_share.dto.response.RideCanceledDto;
import tr.unvercanunlu.ride_share.dto.response.RideCompletedDto;
import tr.unvercanunlu.ride_share.dto.response.RideRequestedDto;
import tr.unvercanunlu.ride_share.entity.Driver;
import tr.unvercanunlu.ride_share.entity.Location;
import tr.unvercanunlu.ride_share.entity.Passenger;
import tr.unvercanunlu.ride_share.entity.Ride;
import tr.unvercanunlu.ride_share.exception.NotFoundException;
import tr.unvercanunlu.ride_share.service.CalculationService;
import tr.unvercanunlu.ride_share.service.EstimationService;
import tr.unvercanunlu.ride_share.service.GeoService;
import tr.unvercanunlu.ride_share.service.RideService;
import tr.unvercanunlu.ride_share.service.ValidationService;
import tr.unvercanunlu.ride_share.status.RideStatus;

public class RideServiceImpl implements RideService {

  private final RideRepository rideRepository;
  private final DriverRepository driverRepository;
  private final GeoService geoService;
  private final CalculationService calculationService;
  private final EstimationService estimationService;
  private final ValidationService validationService;

  public RideServiceImpl(
      RideRepository rideRepository,
      DriverRepository driverRepository,
      GeoService geoService,
      CalculationService calculationService,
      EstimationService estimationService,
      ValidationService validationService
  ) {
    this.rideRepository = rideRepository;
    this.driverRepository = driverRepository;
    this.geoService = geoService;
    this.calculationService = calculationService;
    this.estimationService = estimationService;
    this.validationService = validationService;
  }

  @Override
  public RideRequestedDto request(RequestRideDto request) {
    validationService.checkIdentifier(request.passengerId(), Passenger.class);
    validationService.checkPassengerExists(request.passengerId());
    validationService.checkNoActiveRideForPassenger(request.passengerId());

    Ride ride = Ride.of(request);

    ride.setRequestedAt(LocalDateTime.now());
    ride.setRequestEndAt(ride.getRequestedAt().plusMinutes(AppConfig.MAX_DURATION));
    ride.setDistance(geoService.calculateDistance(request.pickup(), request.dropOff()));
    ride.setFare(calculationService.calculatePrice(ride.getDistance()));

    ride = rideRepository.save(ride);

    Estimation estimation = null;
    if (AppConfig.ESTIMATION) {
      estimation = estimationService.estimate(ride.getPickup(), ride.getDropOff());
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

    List<NearRequestedRideDto> nearRequestedRideDtos = new ArrayList<>();
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

      NearRequestedRideDto nearRequestedRideDto = new NearRequestedRideDto(
          ride.getId(), ride.getPassengerId(), ride.getPickup(), ride.getDropOff(), ride.getDistance(),
          ride.getFare(), ride.getRequestedAt(), ride.getRequestEndAt(), current, distanceToPickup, estimation
      );

      nearRequestedRideDtos.add(nearRequestedRideDto);
    }

    return nearRequestedRideDtos;
  }

  @Override
  public RideAcceptedDto accept(AcceptRideDto request) {
    Ride ride = getDetail(request.rideId());

    validationService.checkRideStatus(Set.of(RideStatus.REQUESTED), ride);
    validationService.checkRideAccepted(ride);
    validationService.checkDriverAvailable(request.driverId());
    validationService.checkNoActiveRideForDriver(request.driverId());

    driverRepository.updateAsBusy(ride.getDriverId());

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
  public DriverApprovedDto approveDriver(UUID rideId) {
    Ride ride = getDetail(rideId);

    validationService.checkRideStatus(Set.of(RideStatus.ACCEPTED), ride);
    validationService.checkDriverPresent(ride);
    validationService.checkDriverExists(ride.getDriverId());

    driverRepository.updateAsBusy(ride.getDriverId());

    ride.setApprovedAt(LocalDateTime.now());
    ride.setStatus(RideStatus.APPROVED);

    ride = rideRepository.save(ride);

    return new DriverApprovedDto(
        ride.getId(), ride.getPassengerId(), ride.getDriverId(), ride.getPickup(), ride.getDropOff(),
        ride.getDistance(), ride.getFare(), ride.getRequestedAt(), ride.getAcceptedAt(), ride.getApprovedAt()
    );
  }

  @Override
  public RideRequestedDto disapproveDriver(UUID rideId) {
    Ride ride = getDetail(rideId);

    validationService.checkRideStatus(Set.of(RideStatus.ACCEPTED), ride);

    Optional.ofNullable(ride.getDriverId())
        .ifPresent(driverRepository::updateAsAvailable);

    ride.setDriverId(null);
    ride.setAcceptedAt(null);
    ride.setStatus(RideStatus.REQUESTED);
    ride.setRequestedAt(LocalDateTime.now());
    ride.setRequestEndAt(ride.getRequestedAt().plusMinutes(AppConfig.MAX_DURATION));

    ride = rideRepository.save(ride);

    Estimation estimation = null;
    if (AppConfig.ESTIMATION) {
      estimation = estimationService.estimate(ride.getPickup(), ride.getDropOff());
    }

    return new RideRequestedDto(
        ride.getId(), ride.getPassengerId(), ride.getPickup(), ride.getDropOff(), ride.getDistance(),
        ride.getFare(), ride.getRequestedAt(), ride.getRequestedAt(), estimation
    );
  }

  @Override
  public PassengerPickupDto pickupPassenger(UUID rideId) {
    Ride ride = getDetail(rideId);

    validationService.checkRideStatus(Set.of(RideStatus.APPROVED), ride);
    validationService.checkDriverPresent(ride);
    validationService.checkDriverExists(ride.getDriverId());

    driverRepository.updateAsBusy(ride.getDriverId());

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
  public RideCompletedDto complete(UUID rideId) {
    Ride ride = getDetail(rideId);

    validationService.checkRideStatus(Set.of(RideStatus.STARTED), ride);
    validationService.checkDriverPresent(ride);
    validationService.checkDriverExists(ride.getDriverId());

    driverRepository.updateAsAvailable(ride.getDriverId());

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

    validationService.checkRideCompleted(ride);

    Optional.ofNullable(ride.getDriverId())
        .ifPresent(driverRepository::updateAsAvailable);

    ride.setCanceledAt(LocalDateTime.now());
    ride.setStatus(RideStatus.CANCELED);

    ride = rideRepository.save(ride);

    driverRepository.updateAsAvailable(ride.getDriverId());

    return new RideCanceledDto(
        ride.getId(), ride.getPassengerId(), ride.getDriverId(), ride.getPickup(), ride.getDropOff(),
        ride.getDistance(), ride.getFare(), ride.getRequestedAt(), ride.getCanceledAt()
    );
  }

  @Override
  public Ride getDetail(UUID rideId) throws NotFoundException {
    validationService.checkIdentifier(rideId, Ride.class);

    return rideRepository.get(rideId)
        .orElseThrow(() -> new NotFoundException(Ride.class, rideId));
  }

  @Override
  public List<Ride> getHistoryOfPassenger(UUID passengerId) {
    validationService.checkIdentifier(passengerId, Passenger.class);

    return rideRepository.getByPassenger(passengerId);
  }

  @Override
  public List<Ride> getHistoryOfDriver(UUID driverId) {
    validationService.checkIdentifier(driverId, Driver.class);

    return rideRepository.getByDriver(driverId);
  }

}
