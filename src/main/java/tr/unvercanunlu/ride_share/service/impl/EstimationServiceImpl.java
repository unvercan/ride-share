package tr.unvercanunlu.ride_share.service.impl;

import static tr.unvercanunlu.ride_share.config.AppConfig.MAX_WAIT;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import tr.unvercanunlu.ride_share.core.log.Logger;
import tr.unvercanunlu.ride_share.dto.response.EstimationDto;
import tr.unvercanunlu.ride_share.entity.Location;
import tr.unvercanunlu.ride_share.helper.LoggerFactory;
import tr.unvercanunlu.ride_share.service.EstimationService;
import tr.unvercanunlu.ride_share.service.GeoService;

@RequiredArgsConstructor
public class EstimationServiceImpl implements EstimationService {

  private static final Logger logger = LoggerFactory.getLogger(EstimationServiceImpl.class);

  private final GeoService geoService;

  @Override
  public EstimationDto estimate(Location pickup, Location dropOff) {
    logger.info(String.format("Estimating duration: pickup=%s, dropOff=%s", pickup, dropOff));

    try {
      int duration = geoService.estimateDuration(pickup, dropOff);
      logger.info(String.format("Estimated duration: %d minutes", duration));
      return new EstimationDto(duration, null, null, null);
    } catch (Exception e) {
      logger.error(String.format("Failed to estimate duration: pickup=%s, dropOff=%s, error=%s", pickup, dropOff, e.getMessage()), e);
      throw e;
    }
  }

  @Override
  public EstimationDto estimate(Location pickup, Location dropOff, Location current, LocalDateTime from) {
    logger.info(String.format("Estimating full trip: pickup=%s, dropOff=%s, current=%s, from=%s", pickup, dropOff, current, from));

    try {
      int duration = geoService.estimateDuration(pickup, dropOff);
      int durationToPickup = geoService.estimateDuration(current, pickup);
      LocalDateTime pickupAt = from.plusMinutes(durationToPickup);
      LocalDateTime pickupEndAt = pickupAt.plus(MAX_WAIT);
      LocalDateTime completedAt = pickupAt.plusMinutes(duration);

      logger.info(
          String.format("Estimation result: duration=%d, pickupAt=%s, pickupEndAt=%s, completedAt=%s", duration, pickupAt, pickupEndAt, completedAt));
      return new EstimationDto(duration, pickupAt, pickupEndAt, completedAt);
    } catch (Exception e) {
      logger.error(String.format("Failed to estimate full trip: pickup=%s, dropOff=%s, current=%s, from=%s, error=%s", pickup, dropOff, current, from,
          e.getMessage()), e);
      throw e;
    }
  }

  @Override
  public EstimationDto estimate(Location pickup, Location dropOff, LocalDateTime pickupAt) {
    logger.info(String.format("Estimating completion: pickup=%s, dropOff=%s, pickupAt=%s", pickup, dropOff, pickupAt));

    try {
      int estimatedDuration = geoService.estimateDuration(pickup, dropOff);
      LocalDateTime estimatedCompletedAt = pickupAt.plusMinutes(estimatedDuration);
      logger.info(String.format("Estimation result: estimatedDuration=%d, estimatedCompletedAt=%s", estimatedDuration, estimatedCompletedAt));
      return new EstimationDto(estimatedDuration, null, null, estimatedCompletedAt);
    } catch (Exception e) {
      logger.error(
          String.format("Failed to estimate completion: pickup=%s, dropOff=%s, pickupAt=%s, error=%s", pickup, dropOff, pickupAt, e.getMessage()), e);
      throw e;
    }
  }

}
