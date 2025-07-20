package tr.unvercanunlu.ride_share.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
import tr.unvercanunlu.ride_share.entity.EntityFactory;
import tr.unvercanunlu.ride_share.entity.Location;
import tr.unvercanunlu.ride_share.entity.Passenger;
import tr.unvercanunlu.ride_share.entity.Ride;
import tr.unvercanunlu.ride_share.exception.NotFoundException;
import tr.unvercanunlu.ride_share.helper.LogHelper;
import tr.unvercanunlu.ride_share.service.CalculationService;
import tr.unvercanunlu.ride_share.service.EstimationService;
import tr.unvercanunlu.ride_share.service.GeoService;
import tr.unvercanunlu.ride_share.service.RideService;
import tr.unvercanunlu.ride_share.service.ValidationService;
import tr.unvercanunlu.ride_share.status.RideStatus;

@RequiredArgsConstructor
public class RideServiceImpl implements RideService {

  private final RideRepository rideRepository;
  private final DriverRepository driverRepository;
  private final GeoService geoService;
  private final CalculationService calculationService;
  private final EstimationService estimationService;
  private final ValidationService validationService;

  @Override
  public RideRequestedDto request(RequestRideDto request) {
    LogHelper.info(this.getClass(),
        String.format("Starting new ride request. passengerId=%s", request.passengerId()));

    try {
      validationService.checkIdentifier(request.passengerId(), Passenger.class);
      validationService.checkPassengerExists(request.passengerId());
      validationService.checkNoActiveRideForPassenger(request.passengerId());

      Ride ride = EntityFactory.from(request);

      ride.setRequestedAt(LocalDateTime.now());
      ride.setRequestEndAt(ride.getRequestedAt().plusMinutes(AppConfig.MAX_DURATION_MINUTES));
      ride.setDistance(geoService.calculateDistance(request.pickup(), request.dropOff()));
      ride.setFare(calculationService.calculatePrice(ride.getDistance()));

      ride = rideRepository.save(ride);

      LogHelper.info(this.getClass(),
          String.format("Ride requested and saved successfully. rideId=%s", ride.getId()));

      Estimation estimation = null;
      if (AppConfig.ESTIMATION) {
        estimation = estimationService.estimate(ride.getPickup(), ride.getDropOff());
      }

      return new RideRequestedDto(
          ride.getId(), ride.getPassengerId(), ride.getPickup(), ride.getDropOff(), ride.getDistance(),
          ride.getFare(), ride.getRequestedAt(), ride.getRequestEndAt(), estimation
      );

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Failed to request ride. passengerId=%s, error=%s", request.passengerId(), e.getMessage()), e);

      throw e;
    }
  }

  @Override
  public List<NearRequestedRideDto> findNearestRequestedRides(Location current) {
    LogHelper.info(this.getClass(),
        String.format("Finding nearest requested rides to location=%s", current));

    LocalDateTime gapStart = LocalDateTime.now();
    LocalDateTime gapEnd = gapStart.plusMinutes(AppConfig.MAX_DURATION_MINUTES);

    List<Ride> requestedRides = rideRepository.getRequestedRidesBetweenGap(gapStart, gapEnd);

    List<NearRequestedRideDto> nearRequestedRideDtos = new ArrayList<>();
    for (Ride ride : requestedRides) {
      double distanceToPickup = geoService.calculateDistance(ride.getPickup(), current);
      if (distanceToPickup > AppConfig.NEAR_DISTANCE_KM) {
        continue;
      }

      Estimation estimation = null;
      if (AppConfig.ESTIMATION) {
        estimation = estimationService.estimate(ride.getPickup(), ride.getDropOff(), current, gapStart);
        if (estimation.pickupAt().isAfter(ride.getRequestEndAt())) {
          continue;
        }
      } else {
        if (ride.getRequestEndAt().isBefore(gapStart)) {
          continue;
        }
      }

      NearRequestedRideDto nearRequestedRideDto = new NearRequestedRideDto(
          ride.getId(), ride.getPassengerId(), ride.getPickup(), ride.getDropOff(), ride.getDistance(),
          ride.getFare(), ride.getRequestedAt(), ride.getRequestEndAt(), current, distanceToPickup, estimation
      );

      nearRequestedRideDtos.add(nearRequestedRideDto);
    }

    LogHelper.info(this.getClass(),
        String.format("Found %d nearby requested rides for location=%s", nearRequestedRideDtos.size(), current));

    return nearRequestedRideDtos;
  }

  @Override
  public RideAcceptedDto accept(AcceptRideDto request) {
    LogHelper.info(this.getClass(),
        String.format("Starting ride acceptance. rideId=%s, driverId=%s", request.rideId(), request.driverId()));

    try {
      Ride ride = getDetail(request.rideId());

      validationService.checkRideTransition(ride, RideStatus.ACCEPTED);
      validationService.checkRideAccepted(ride);
      validationService.checkDriverAvailable(request.driverId());
      validationService.checkNoActiveRideForDriver(request.driverId());

      ride.setDriverId(request.driverId());
      ride.setAcceptedAt(LocalDateTime.now());
      ride.setStatus(RideStatus.ACCEPTED);

      ride = rideRepository.save(ride);

      driverRepository.updateAsBusy(ride.getDriverId());

      LogHelper.info(this.getClass(),
          String.format("Successfully accepted ride. rideId=%s, driverId=%s", ride.getId(), ride.getDriverId()));

      Estimation estimation = null;
      if (AppConfig.ESTIMATION) {
        estimation = estimationService.estimate(ride.getPickup(), ride.getDropOff(), request.current(), ride.getAcceptedAt());
      }

      return new RideAcceptedDto(
          ride.getId(), ride.getPassengerId(), ride.getDriverId(), ride.getPickup(), ride.getDropOff(),
          ride.getDistance(), ride.getFare(), ride.getRequestedAt(), ride.getAcceptedAt(), estimation
      );

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Failed to accept ride. rideId=%s, driverId=%s, error=%s", request.rideId(), request.driverId(), e.getMessage()), e);

      throw e;
    }
  }

  @Override
  public DriverApprovedDto approveDriver(UUID rideId) {
    LogHelper.info(this.getClass(),
        String.format("Approving ride. rideId=%s", rideId));

    try {
      Ride ride = getDetail(rideId);

      validationService.checkRideTransition(ride, RideStatus.APPROVED);
      validationService.checkDriverPresent(ride);
      validationService.checkDriverExists(ride.getDriverId());

      ride.setApprovedAt(LocalDateTime.now());
      ride.setStatus(RideStatus.APPROVED);

      ride = rideRepository.save(ride);

      driverRepository.updateAsBusy(ride.getDriverId());

      LogHelper.info(this.getClass(),
          String.format("Ride approved by passenger. rideId=%s", ride.getId()));

      return new DriverApprovedDto(
          ride.getId(), ride.getPassengerId(), ride.getDriverId(), ride.getPickup(), ride.getDropOff(),
          ride.getDistance(), ride.getFare(), ride.getRequestedAt(), ride.getAcceptedAt(), ride.getApprovedAt()
      );

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Failed to approve ride. rideId=%s, error=%s", rideId, e.getMessage()), e);

      throw e;
    }
  }

  @Override
  public RideRequestedDto disapproveDriver(UUID rideId) {
    LogHelper.info(this.getClass(),
        String.format("Disapproving ride. rideId=%s", rideId));

    try {
      Ride ride = getDetail(rideId);

      validationService.checkRideTransition(ride, RideStatus.REQUESTED);

      UUID previousDriverId = ride.getDriverId();

      ride.setDriverId(null);
      ride.setAcceptedAt(null);
      ride.setStatus(RideStatus.REQUESTED);
      ride.setRequestEndAt(LocalDateTime.now().plusMinutes(AppConfig.MAX_DURATION_MINUTES));

      ride = rideRepository.save(ride);

      Optional.ofNullable(previousDriverId)
          .ifPresent(driverRepository::updateAsAvailable);

      LogHelper.info(this.getClass(),
          String.format("Ride disapproved by passenger. rideId=%s", ride.getId()));

      Estimation estimation = null;
      if (AppConfig.ESTIMATION) {
        estimation = estimationService.estimate(ride.getPickup(), ride.getDropOff());
      }

      return new RideRequestedDto(
          ride.getId(), ride.getPassengerId(), ride.getPickup(), ride.getDropOff(), ride.getDistance(),
          ride.getFare(), ride.getRequestedAt(), ride.getRequestEndAt(), estimation
      );

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Failed to disapprove ride. rideId=%s, error=%s", rideId, e.getMessage()), e);

      throw e;
    }
  }

  @Override
  public PassengerPickupDto pickupPassenger(UUID rideId) {
    LogHelper.info(this.getClass(),
        String.format("Picking up passenger. rideId=%s", rideId));

    try {
      Ride ride = getDetail(rideId);

      validationService.checkRideTransition(ride, RideStatus.STARTED);
      validationService.checkDriverPresent(ride);
      validationService.checkDriverExists(ride.getDriverId());

      ride.setPickupAt(LocalDateTime.now());
      ride.setPickupEndAt(ride.getPickupAt().plusMinutes(AppConfig.MAX_DURATION_MINUTES));
      ride.setStatus(RideStatus.STARTED);

      ride = rideRepository.save(ride);

      driverRepository.updateAsBusy(ride.getDriverId());

      LogHelper.info(this.getClass(),
          String.format("Passenger picked up by driver. rideId=%s", ride.getId()));

      Estimation estimation = null;
      if (AppConfig.ESTIMATION) {
        estimation = estimationService.estimate(ride.getPickup(), ride.getDropOff(), ride.getPickupAt());
      }

      return new PassengerPickupDto(
          ride.getId(), ride.getPassengerId(), ride.getDriverId(), ride.getPickup(), ride.getDropOff(), ride.getDistance(),
          ride.getFare(), ride.getRequestedAt(), ride.getAcceptedAt(), ride.getPickupAt(), estimation
      );

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Failed to pickup passenger. rideId=%s, error=%s", rideId, e.getMessage()), e);

      throw e;
    }
  }

  @Override
  public RideCompletedDto complete(UUID rideId) {
    LogHelper.info(this.getClass(),
        String.format("Completing ride. rideId=%s", rideId));

    try {
      Ride ride = getDetail(rideId);

      validationService.checkRideTransition(ride, RideStatus.COMPLETED);
      validationService.checkDriverPresent(ride);
      validationService.checkDriverExists(ride.getDriverId());

      ride.setCompletedAt(LocalDateTime.now());
      ride.setDuration(Duration.between(ride.getPickupAt(), ride.getCompletedAt()).toMinutes());
      ride.setStatus(RideStatus.COMPLETED);

      ride = rideRepository.save(ride);

      driverRepository.updateAsAvailable(ride.getDriverId());

      LogHelper.info(this.getClass(),
          String.format("Ride completed. rideId=%s", ride.getId()));

      return new RideCompletedDto(
          ride.getId(), ride.getPassengerId(), ride.getDriverId(), ride.getPickup(), ride.getDropOff(), ride.getDistance(),
          ride.getFare(), ride.getRequestedAt(), ride.getAcceptedAt(), ride.getPickupAt(), ride.getCompletedAt(), ride.getDuration()
      );

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Failed to complete ride. rideId=%s, error=%s", rideId, e.getMessage()), e);

      throw e;
    }
  }

  @Override
  public RideCanceledDto cancel(UUID rideId) {
    LogHelper.info(this.getClass(),
        String.format("Canceling ride. rideId=%s", rideId));

    try {
      Ride ride = getDetail(rideId);

      validationService.checkRideTransition(ride, RideStatus.CANCELED);

      ride.setCanceledAt(LocalDateTime.now());
      ride.setStatus(RideStatus.CANCELED);

      ride = rideRepository.save(ride);

      Optional.ofNullable(ride.getDriverId())
          .ifPresent(driverRepository::updateAsAvailable);

      LogHelper.info(this.getClass(),
          String.format("Ride cancelled. rideId=%s", ride.getId()));

      return new RideCanceledDto(
          ride.getId(), ride.getPassengerId(), ride.getDriverId(), ride.getPickup(), ride.getDropOff(),
          ride.getDistance(), ride.getFare(), ride.getRequestedAt(), ride.getCanceledAt()
      );

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Failed to cancel ride. rideId=%s, error=%s", rideId, e.getMessage()), e);

      throw e;
    }
  }

  @Override
  public Ride getDetail(UUID rideId) throws NotFoundException {
    LogHelper.info(this.getClass(),
        String.format("Getting ride detail. rideId=%s", rideId));

    try {
      validationService.checkIdentifier(rideId, Ride.class);

      Ride ride = rideRepository.get(rideId)
          .orElseThrow(() -> new NotFoundException(Ride.class, rideId));

      LogHelper.info(this.getClass(),
          String.format("Ride detail retrieved. rideId=%s", rideId));

      return ride;

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Failed to get ride detail. rideId=%s, error=%s", rideId, e.getMessage()), e);

      throw e;
    }
  }

  @Override
  public List<Ride> getHistoryOfPassenger(UUID passengerId) {
    LogHelper.info(this.getClass(),
        String.format("Getting ride history for passengerId=%s", passengerId));

    List<Ride> rides = rideRepository.getByPassenger(passengerId);

    LogHelper.info(this.getClass(),
        String.format("Found %d rides for passengerId=%s", rides.size(), passengerId));

    return rides;
  }

  @Override
  public List<Ride> getHistoryOfDriver(UUID driverId) {
    LogHelper.info(this.getClass(),
        String.format("Getting ride history for driverId=%s", driverId));

    List<Ride> rides = rideRepository.getByDriver(driverId);

    LogHelper.info(this.getClass(),
        String.format("Found %d rides for driverId=%s", rides.size(), driverId));

    return rides;
  }

}
