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
import tr.unvercanunlu.ride_share.core.log.Logger;
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
import tr.unvercanunlu.ride_share.helper.LoggerFactory;
import tr.unvercanunlu.ride_share.service.CalculationService;
import tr.unvercanunlu.ride_share.service.EstimationService;
import tr.unvercanunlu.ride_share.service.GeoService;
import tr.unvercanunlu.ride_share.service.RideService;
import tr.unvercanunlu.ride_share.service.ValidationService;
import tr.unvercanunlu.ride_share.status.RideStatus;

@RequiredArgsConstructor
public class RideServiceImpl implements RideService {

  private static final Logger logger = LoggerFactory.getLogger(RideServiceImpl.class);

  private final RideRepository rideRepository;
  private final DriverRepository driverRepository;
  private final GeoService geoService;
  private final CalculationService calculationService;
  private final EstimationService estimationService;
  private final ValidationService validationService;


  @Override
  public RideRequestedDto requestRide(RequestRideDto request) {
    logger.info(String.format("Starting new ride request. passengerId=%s", request.passengerId()));

    try {
      validationService.checkIdentifier(request.passengerId(), Passenger.class);
      validationService.checkPassengerExists(request.passengerId());
      validationService.checkNoActiveRideForPassenger(request.passengerId());

      Ride ride = EntityFactory.from(request);
      ride.setRequestedAt(LocalDateTime.now());
      ride.setRequestEndAt(ride.getRequestedAt().plus(MAX_WAIT));
      ride.setDistance(geoService.calculateDistance(request.pickup(), request.dropOff()));
      ride.setFare(calculationService.calculatePrice(ride.getDistance()));
      ride = rideRepository.save(ride);
      logger.info(String.format("Ride requested and saved successfully. rideId=%s", ride.getId()));

      EstimationDto estimation = null;
      if (ESTIMATION_ENABLED) {
        estimation = estimationService.estimate(ride.getPickup(), ride.getDropOff());
      }

      return new RideRequestedDto(
          ride.getId(), ride.getPassengerId(), ride.getPickup(), ride.getDropOff(), ride.getDistance(),
          ride.getFare(), ride.getRequestedAt(), ride.getRequestEndAt(), estimation
      );
    } catch (Exception e) {
      logger.error(String.format("Failed to request ride. passengerId=%s, error=%s", request.passengerId(), e.getMessage()), e);
      throw e;
    }
  }

  @Override
  public List<NearRequestedRideDto> findNearestRequestedRides(Location current) {
    logger.info(String.format("Finding nearest requested rides to location=%s", current));
    LocalDateTime windowStart = LocalDateTime.now();
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

    logger.info(String.format("Found %d nearby requested rides for location=%s", nearRequestedRides.size(), current));
    return nearRequestedRides;
  }

  @Override
  public RideAcceptedDto acceptRide(AcceptRideDto request) {
    logger.info(String.format("Starting ride acceptance. rideId=%s, driverId=%s", request.rideId(), request.driverId()));

    try {
      Ride ride = getRide(request.rideId());

      validationService.checkRideTransition(ride, RideStatus.ACCEPTED);
      validationService.checkRideAccepted(ride);
      validationService.checkDriverAvailable(request.driverId());
      validationService.checkNoActiveRideForDriver(request.driverId());

      ride.setDriverId(request.driverId());
      ride.setAcceptedAt(LocalDateTime.now());
      ride.setStatus(RideStatus.ACCEPTED);

      ride = rideRepository.save(ride);
      driverRepository.setBusy(ride.getDriverId());
      logger.info(String.format("Successfully accepted ride. rideId=%s, driverId=%s", ride.getId(), ride.getDriverId()));

      EstimationDto estimation = null;
      if (ESTIMATION_ENABLED) {
        estimation = estimationService.estimate(ride.getPickup(), ride.getDropOff(), request.current(), ride.getAcceptedAt());
      }

      return new RideAcceptedDto(
          ride.getId(), ride.getPassengerId(), ride.getDriverId(), ride.getPickup(), ride.getDropOff(),
          ride.getDistance(), ride.getFare(), ride.getRequestedAt(), ride.getAcceptedAt(), estimation
      );

    } catch (Exception e) {
      logger.error(String.format("Failed to accept ride. rideId=%s, driverId=%s, error=%s", request.rideId(), request.driverId(), e.getMessage()), e);
      throw e;
    }
  }

  @Override
  public DriverApprovedDto approveAssignedDriver(UUID rideId) {
    logger.info(String.format("Approving ride. rideId=%s", rideId));

    try {
      Ride ride = getRide(rideId);

      validationService.checkRideTransition(ride, RideStatus.APPROVED);
      validationService.checkDriverPresent(ride);
      validationService.checkDriverExists(ride.getDriverId());

      ride.setApprovedAt(LocalDateTime.now());
      ride.setStatus(RideStatus.APPROVED);
      ride = rideRepository.save(ride);

      driverRepository.setBusy(ride.getDriverId());
      logger.info(String.format("Ride approved by passenger. rideId=%s", ride.getId()));

      return new DriverApprovedDto(
          ride.getId(), ride.getPassengerId(), ride.getDriverId(), ride.getPickup(), ride.getDropOff(),
          ride.getDistance(), ride.getFare(), ride.getRequestedAt(), ride.getAcceptedAt(), ride.getApprovedAt()
      );

    } catch (Exception e) {
      logger.error(String.format("Failed to approve ride. rideId=%s, error=%s", rideId, e.getMessage()), e);
      throw e;
    }
  }

  @Override
  public RideRequestedDto rejectAssignedDriver(UUID rideId) {
    logger.info(String.format("Disapproving ride. rideId=%s", rideId));

    try {
      Ride ride = getRide(rideId);
      validationService.checkRideTransition(ride, RideStatus.REQUESTED);
      UUID previousDriverId = ride.getDriverId();
      ride.setDriverId(null);
      ride.setAcceptedAt(null);
      ride.setStatus(RideStatus.REQUESTED);
      ride.setRequestEndAt(LocalDateTime.now().plus(MAX_WAIT));
      ride = rideRepository.save(ride);

      Optional.ofNullable(previousDriverId).ifPresent(driverRepository::setAvailable);
      logger.info(String.format("Ride disapproved by passenger. rideId=%s", ride.getId()));

      EstimationDto estimation = null;
      if (ESTIMATION_ENABLED) {
        estimation = estimationService.estimate(ride.getPickup(), ride.getDropOff());
      }

      return new RideRequestedDto(
          ride.getId(), ride.getPassengerId(), ride.getPickup(), ride.getDropOff(), ride.getDistance(),
          ride.getFare(), ride.getRequestedAt(), ride.getRequestEndAt(), estimation
      );

    } catch (Exception e) {
      logger.error(String.format("Failed to disapprove ride. rideId=%s, error=%s", rideId, e.getMessage()), e);
      throw e;
    }
  }

  @Override
  public PassengerPickupDto startTrip(UUID rideId) {
    logger.info(String.format("Picking up passenger. rideId=%s", rideId));

    try {
      Ride ride = getRide(rideId);

      validationService.checkRideTransition(ride, RideStatus.STARTED);
      validationService.checkDriverPresent(ride);
      validationService.checkDriverExists(ride.getDriverId());

      ride.setPickupAt(LocalDateTime.now());
      ride.setPickupEndAt(ride.getPickupAt().plus(MAX_WAIT));
      ride.setStatus(RideStatus.STARTED);
      ride = rideRepository.save(ride);

      driverRepository.setBusy(ride.getDriverId());
      logger.info(String.format("Passenger picked up by driver. rideId=%s", ride.getId()));

      EstimationDto estimation = null;
      if (ESTIMATION_ENABLED) {
        estimation = estimationService.estimate(ride.getPickup(), ride.getDropOff(), ride.getPickupAt());
      }

      return new PassengerPickupDto(
          ride.getId(), ride.getPassengerId(), ride.getDriverId(), ride.getPickup(), ride.getDropOff(), ride.getDistance(),
          ride.getFare(), ride.getRequestedAt(), ride.getAcceptedAt(), ride.getPickupAt(), estimation
      );

    } catch (Exception e) {
      logger.error(String.format("Failed to pickup passenger. rideId=%s, error=%s", rideId, e.getMessage()), e);

      throw e;
    }
  }

  @Override
  public RideCompletedDto completeTrip(UUID rideId) {
    logger.info(String.format("Completing ride. rideId=%s", rideId));

    try {
      Ride ride = getRide(rideId);

      validationService.checkRideTransition(ride, RideStatus.COMPLETED);
      validationService.checkDriverPresent(ride);
      validationService.checkDriverExists(ride.getDriverId());

      ride.setCompletedAt(LocalDateTime.now());
      ride.setDuration(Duration.between(ride.getPickupAt(), ride.getCompletedAt()).toMinutes());
      ride.setStatus(RideStatus.COMPLETED);
      ride = rideRepository.save(ride);

      driverRepository.setAvailable(ride.getDriverId());
      logger.info(String.format("Ride completed. rideId=%s", ride.getId()));

      return new RideCompletedDto(
          ride.getId(), ride.getPassengerId(), ride.getDriverId(), ride.getPickup(), ride.getDropOff(), ride.getDistance(),
          ride.getFare(), ride.getRequestedAt(), ride.getAcceptedAt(), ride.getPickupAt(), ride.getCompletedAt(), ride.getDuration()
      );

    } catch (Exception e) {
      logger.error(String.format("Failed to complete ride. rideId=%s, error=%s", rideId, e.getMessage()), e);
      throw e;
    }
  }

  @Override
  public RideCanceledDto cancelRide(UUID rideId) {
    logger.info(String.format("Canceling ride. rideId=%s", rideId));

    try {
      Ride ride = getRide(rideId);
      validationService.checkRideTransition(ride, RideStatus.CANCELED);
      ride.setCanceledAt(LocalDateTime.now());
      ride.setStatus(RideStatus.CANCELED);
      ride = rideRepository.save(ride);
      Optional.ofNullable(ride.getDriverId()).ifPresent(driverRepository::setAvailable);
      logger.info(String.format("Ride cancelled. rideId=%s", ride.getId()));

      return new RideCanceledDto(
          ride.getId(), ride.getPassengerId(), ride.getDriverId(), ride.getPickup(), ride.getDropOff(),
          ride.getDistance(), ride.getFare(), ride.getRequestedAt(), ride.getCanceledAt()
      );
    } catch (Exception e) {
      logger.error(String.format("Failed to cancel ride. rideId=%s, error=%s", rideId, e.getMessage()), e);
      throw e;
    }
  }

  @Override
  public Ride getRide(UUID rideId) throws NotFoundException {
    logger.info(String.format("Getting ride detail. rideId=%s", rideId));
    try {
      validationService.checkIdentifier(rideId, Ride.class);
      Ride ride = rideRepository.findById(rideId).orElseThrow(() -> new NotFoundException(Ride.class, rideId));
      logger.info(String.format("Ride detail retrieved. rideId=%s", rideId));
      return ride;
    } catch (Exception e) {
      logger.error(String.format("Failed to get ride detail. rideId=%s, error=%s", rideId, e.getMessage()), e);
      throw e;
    }
  }

  @Override
  public List<Ride> getHistoryOfPassenger(UUID passengerId) {
    logger.info(String.format("Getting ride history for passengerId=%s", passengerId));
    List<Ride> rides = rideRepository.findAllByPassengerId(passengerId);
    logger.info(String.format("Found %d rides for passengerId=%s", rides.size(), passengerId));
    return rides;
  }

  @Override
  public List<Ride> getHistoryOfDriver(UUID driverId) {
    logger.info(String.format("Getting ride history for driverId=%s", driverId));
    List<Ride> rides = rideRepository.findAllByDriverId(driverId);
    logger.info(String.format("Found %d rides for driverId=%s", rides.size(), driverId));
    return rides;
  }

}
