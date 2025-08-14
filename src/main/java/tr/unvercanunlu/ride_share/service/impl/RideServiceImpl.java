package tr.unvercanunlu.ride_share.service.impl;

import static tr.unvercanunlu.ride_share.config.AppConfig.ESTIMATION_ENABLED;
import static tr.unvercanunlu.ride_share.config.AppConfig.MAX_PICKUP_RADIUS_KM;
import static tr.unvercanunlu.ride_share.config.AppConfig.MAX_WAIT;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tr.unvercanunlu.ride_share.dao.DriverRepository;
import tr.unvercanunlu.ride_share.dao.RideRepository;
import tr.unvercanunlu.ride_share.dto.request.AcceptRideDto;
import tr.unvercanunlu.ride_share.dto.request.RequestRideDto;
import tr.unvercanunlu.ride_share.dto.response.DriverApprovedDto;
import tr.unvercanunlu.ride_share.dto.response.EstimationDto;
import tr.unvercanunlu.ride_share.dto.response.NearRequestedRideDto;
import tr.unvercanunlu.ride_share.dto.response.PassengerPickupDto;
import tr.unvercanunlu.ride_share.dto.response.RideAcceptedDto;
import tr.unvercanunlu.ride_share.dto.response.RideCanceledDto;
import tr.unvercanunlu.ride_share.dto.response.RideCompletedDto;
import tr.unvercanunlu.ride_share.dto.response.RideRequestedDto;
import tr.unvercanunlu.ride_share.entity.EntityFactory;
import tr.unvercanunlu.ride_share.entity.Location;
import tr.unvercanunlu.ride_share.entity.Passenger;
import tr.unvercanunlu.ride_share.entity.Ride;
import tr.unvercanunlu.ride_share.exception.NotFoundException;
import tr.unvercanunlu.ride_share.helper.TimeHelper;
import tr.unvercanunlu.ride_share.service.CalculationService;
import tr.unvercanunlu.ride_share.service.EstimationService;
import tr.unvercanunlu.ride_share.service.GeoService;
import tr.unvercanunlu.ride_share.service.RideService;
import tr.unvercanunlu.ride_share.service.ValidationService;
import tr.unvercanunlu.ride_share.status.RideStatus;

@Slf4j
@RequiredArgsConstructor
public class RideServiceImpl implements RideService {

  private final RideRepository rideRepository;
  private final DriverRepository driverRepository;
  private final GeoService geoService;
  private final CalculationService calculationService;
  private final EstimationService estimationService;
  private final ValidationService validationService;


  @Override
  public RideRequestedDto requestRide(RequestRideDto request) {
    log.info("Starting new ride request. passengerId={}", request.passengerId());

    try {
      validationService.checkIdentifier(request.passengerId(), Passenger.class);
      validationService.checkPassengerExists(request.passengerId());
      validationService.checkNoActiveRideForPassenger(request.passengerId());

      Ride ride = EntityFactory.from(request);
      ride.setRequestedAt(TimeHelper.now());
      ride.setRequestEndAt(ride.getRequestedAt().plus(MAX_WAIT));
      ride.setDistance(geoService.calculateDistance(request.pickup(), request.dropOff()));
      ride.setFare(calculationService.calculatePrice(ride.getDistance()));
      ride = rideRepository.save(ride);
      log.info("Ride requested and saved successfully. rideId={}", ride.getId());

      EstimationDto estimation = null;
      if (ESTIMATION_ENABLED) {
        estimation = estimationService.estimate(ride.getPickup(), ride.getDropOff());
      }

      return new RideRequestedDto(
          ride.getId(), ride.getPassengerId(), ride.getPickup(), ride.getDropOff(), ride.getDistance(),
          ride.getFare(), ride.getRequestedAt(), ride.getRequestEndAt(), estimation
      );
    } catch (Exception e) {
      log.error("Failed to request ride. passengerId={}, error={}", request.passengerId(), e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public List<NearRequestedRideDto> findNearestRequestedRides(Location current) {
    log.info("Finding nearest requested rides to location={}", current);
    LocalDateTime windowStart = TimeHelper.now();
    LocalDateTime windowEnd = windowStart.plus(MAX_WAIT);
    List<Ride> requestedRides = rideRepository.findRequestedWithinWindow(windowStart, windowEnd);
    List<NearRequestedRideDto> nearRequestedRides = new ArrayList<>();

    for (Ride ride : requestedRides) {
      double distanceToPickup = geoService.calculateDistance(ride.getPickup(), current);
      if (distanceToPickup > MAX_PICKUP_RADIUS_KM) {
        continue;
      }

      EstimationDto estimation = null;
      if (ESTIMATION_ENABLED) {
        estimation = estimationService.estimate(ride.getPickup(), ride.getDropOff(), current, windowStart);
        if (estimation.pickupAt().isAfter(ride.getRequestEndAt())) {
          continue;
        }
      } else {
        if (ride.getRequestEndAt().isBefore(windowStart)) {
          continue;
        }
      }

      NearRequestedRideDto nearRequestedRide = new NearRequestedRideDto(
          ride.getId(), ride.getPassengerId(), ride.getPickup(), ride.getDropOff(), ride.getDistance(),
          ride.getFare(), ride.getRequestedAt(), ride.getRequestEndAt(), current, distanceToPickup, estimation
      );
      nearRequestedRides.add(nearRequestedRide);
    }

    log.info("Found {} nearby requested rides for location={}", nearRequestedRides.size(), current);
    return nearRequestedRides;
  }

  @Override
  public RideAcceptedDto acceptRide(AcceptRideDto request) {
    log.info("Starting ride acceptance. rideId={}, driverId={}", request.rideId(), request.driverId());

    try {
      Ride ride = getRide(request.rideId());

      validationService.checkRideTransition(ride, RideStatus.ACCEPTED);
      validationService.checkRideAccepted(ride);
      validationService.checkDriverAvailable(request.driverId());
      validationService.checkNoActiveRideForDriver(request.driverId());

      ride.setDriverId(request.driverId());
      ride.setAcceptedAt(TimeHelper.now());
      ride.setStatus(RideStatus.ACCEPTED);

      ride = rideRepository.save(ride);
      driverRepository.setBusy(ride.getDriverId());
      log.info("Successfully accepted ride. rideId={}, driverId={}", ride.getId(), ride.getDriverId());

      EstimationDto estimation = null;
      if (ESTIMATION_ENABLED) {
        estimation = estimationService.estimate(ride.getPickup(), ride.getDropOff(), request.current(), ride.getAcceptedAt());
      }

      return new RideAcceptedDto(
          ride.getId(), ride.getPassengerId(), ride.getDriverId(), ride.getPickup(), ride.getDropOff(),
          ride.getDistance(), ride.getFare(), ride.getRequestedAt(), ride.getAcceptedAt(), estimation
      );

    } catch (Exception e) {
      log.error("Failed to accept ride. rideId={}, driverId={}, error={}", request.rideId(), request.driverId(), e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public DriverApprovedDto approveAssignedDriver(UUID rideId) {
    log.info("Approving ride. rideId={}", rideId);

    try {
      Ride ride = getRide(rideId);

      validationService.checkRideTransition(ride, RideStatus.APPROVED);
      validationService.checkDriverPresent(ride);
      validationService.checkDriverExists(ride.getDriverId());

      ride.setApprovedAt(TimeHelper.now());
      ride.setStatus(RideStatus.APPROVED);
      ride = rideRepository.save(ride);

      driverRepository.setBusy(ride.getDriverId());
      log.info("Ride approved by passenger. rideId={}", ride.getId());

      return new DriverApprovedDto(
          ride.getId(), ride.getPassengerId(), ride.getDriverId(), ride.getPickup(), ride.getDropOff(),
          ride.getDistance(), ride.getFare(), ride.getRequestedAt(), ride.getAcceptedAt(), ride.getApprovedAt()
      );

    } catch (Exception e) {
      log.error("Failed to approve ride. rideId={}, error={}", rideId, e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public RideRequestedDto rejectAssignedDriver(UUID rideId) {
    log.info("Disapproving ride. rideId={}", rideId);

    try {
      Ride ride = getRide(rideId);
      validationService.checkRideTransition(ride, RideStatus.REQUESTED);
      UUID previousDriverId = ride.getDriverId();
      ride.setDriverId(null);
      ride.setAcceptedAt(null);
      ride.setStatus(RideStatus.REQUESTED);
      ride.setRequestEndAt(TimeHelper.now().plus(MAX_WAIT));
      ride = rideRepository.save(ride);

      Optional.ofNullable(previousDriverId).ifPresent(driverRepository::setAvailable);
      log.info("Ride disapproved by passenger. rideId={}", ride.getId());

      EstimationDto estimation = null;
      if (ESTIMATION_ENABLED) {
        estimation = estimationService.estimate(ride.getPickup(), ride.getDropOff());
      }

      return new RideRequestedDto(
          ride.getId(), ride.getPassengerId(), ride.getPickup(), ride.getDropOff(), ride.getDistance(),
          ride.getFare(), ride.getRequestedAt(), ride.getRequestEndAt(), estimation
      );

    } catch (Exception e) {
      log.error("Failed to disapprove ride. rideId={}, error={}", rideId, e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public PassengerPickupDto startTrip(UUID rideId) {
    log.info("Picking up passenger. rideId={}", rideId);

    try {
      Ride ride = getRide(rideId);

      validationService.checkRideTransition(ride, RideStatus.STARTED);
      validationService.checkDriverPresent(ride);
      validationService.checkDriverExists(ride.getDriverId());

      ride.setPickupAt(TimeHelper.now());
      ride.setPickupEndAt(ride.getPickupAt().plus(MAX_WAIT));
      ride.setStatus(RideStatus.STARTED);
      ride = rideRepository.save(ride);

      driverRepository.setBusy(ride.getDriverId());
      log.info("Passenger picked up by driver. rideId={}", ride.getId());

      EstimationDto estimation = null;
      if (ESTIMATION_ENABLED) {
        estimation = estimationService.estimate(ride.getPickup(), ride.getDropOff(), ride.getPickupAt());
      }

      return new PassengerPickupDto(
          ride.getId(), ride.getPassengerId(), ride.getDriverId(), ride.getPickup(), ride.getDropOff(), ride.getDistance(),
          ride.getFare(), ride.getRequestedAt(), ride.getAcceptedAt(), ride.getPickupAt(), estimation
      );

    } catch (Exception e) {
      log.error("Failed to pickup passenger. rideId={}, error={}", rideId, e.getMessage(), e);

      throw e;
    }
  }

  @Override
  public RideCompletedDto completeTrip(UUID rideId) {
    log.info("Completing ride. rideId={}", rideId);

    try {
      Ride ride = getRide(rideId);

      validationService.checkRideTransition(ride, RideStatus.COMPLETED);
      validationService.checkDriverPresent(ride);
      validationService.checkDriverExists(ride.getDriverId());

      ride.setCompletedAt(TimeHelper.now());
      ride.setDuration(Duration.between(ride.getPickupAt(), ride.getCompletedAt()).toMinutes());
      ride.setStatus(RideStatus.COMPLETED);
      ride = rideRepository.save(ride);

      driverRepository.setAvailable(ride.getDriverId());
      log.info("Ride completed. rideId={}", ride.getId());

      return new RideCompletedDto(
          ride.getId(), ride.getPassengerId(), ride.getDriverId(), ride.getPickup(), ride.getDropOff(), ride.getDistance(),
          ride.getFare(), ride.getRequestedAt(), ride.getAcceptedAt(), ride.getPickupAt(), ride.getCompletedAt(), ride.getDuration()
      );

    } catch (Exception e) {
      log.error("Failed to complete ride. rideId={}, error={}", rideId, e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public RideCanceledDto cancelRide(UUID rideId) {
    log.info("Canceling ride. rideId={}", rideId);

    try {
      Ride ride = getRide(rideId);
      validationService.checkRideTransition(ride, RideStatus.CANCELED);
      ride.setCanceledAt(TimeHelper.now());
      ride.setStatus(RideStatus.CANCELED);
      ride = rideRepository.save(ride);
      Optional.ofNullable(ride.getDriverId()).ifPresent(driverRepository::setAvailable);
      log.info("Ride cancelled. rideId={}", ride.getId());

      return new RideCanceledDto(
          ride.getId(), ride.getPassengerId(), ride.getDriverId(), ride.getPickup(), ride.getDropOff(),
          ride.getDistance(), ride.getFare(), ride.getRequestedAt(), ride.getCanceledAt()
      );
    } catch (Exception e) {
      log.error("Failed to cancel ride. rideId={}, error={}", rideId, e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public Ride getRide(UUID rideId) throws NotFoundException {
    log.info("Getting ride detail. rideId={}", rideId);
    try {
      validationService.checkIdentifier(rideId, Ride.class);
      Ride ride = rideRepository.findById(rideId).orElseThrow(() -> new NotFoundException(Ride.class, rideId));
      log.info("Ride detail retrieved. rideId={}", rideId);
      return ride;
    } catch (Exception e) {
      log.error("Failed to get ride detail. rideId={}, error={}", rideId, e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public List<Ride> getHistoryOfPassenger(UUID passengerId) {
    log.info("Getting ride history for passengerId={}", passengerId);
    List<Ride> rides = rideRepository.findAllByPassengerId(passengerId);
    log.info("Found {} rides for passengerId={}", rides.size(), passengerId);
    return rides;
  }

  @Override
  public List<Ride> getHistoryOfDriver(UUID driverId) {
    log.info("Getting ride history for driverId={}", driverId);
    List<Ride> rides = rideRepository.findAllByDriverId(driverId);
    log.info("Found {} rides for driverId={}", rides.size(), driverId);
    return rides;
  }

}
