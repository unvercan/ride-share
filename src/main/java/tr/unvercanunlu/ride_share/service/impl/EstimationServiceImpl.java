package tr.unvercanunlu.ride_share.service.impl;

import static tr.unvercanunlu.ride_share.config.AppConfig.MAX_WAIT;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tr.unvercanunlu.ride_share.dto.response.EstimationDto;
import tr.unvercanunlu.ride_share.entity.Location;
import tr.unvercanunlu.ride_share.service.EstimationService;
import tr.unvercanunlu.ride_share.service.GeoService;

@Slf4j
@RequiredArgsConstructor
public class EstimationServiceImpl implements EstimationService {

  private final GeoService geoService;

  @Override
  public EstimationDto estimate(Location pickup, Location dropOff) {
    log.info("Estimating duration: pickup={}, dropOff={}", pickup, dropOff);
    try {
      int duration = geoService.estimateDuration(pickup, dropOff);
      log.info("Estimated duration: {} minutes", duration);
      return new EstimationDto(duration, null, null, null);
    } catch (Exception ex) {
      log.error("Failed to estimate duration: pickup={}, dropOff={}", pickup, dropOff, ex);
      throw ex;
    }
  }

  @Override
  public EstimationDto estimate(Location pickup, Location dropOff, Location current, LocalDateTime from) {
    log.info("Estimating full trip: pickup={}, dropOff={}, current={}, from={}", pickup, dropOff, current, from);
    try {
      int duration = geoService.estimateDuration(pickup, dropOff);
      int durationToPickup = geoService.estimateDuration(current, pickup);
      LocalDateTime pickupAt = from.plusMinutes(durationToPickup);
      LocalDateTime pickupEndAt = pickupAt.plus(MAX_WAIT);
      LocalDateTime completedAt = pickupAt.plusMinutes(duration);
      log.info("Estimation result: duration={}, pickupAt={}, pickupEndAt={}, completedAt={}",
          duration, pickupAt, pickupEndAt, completedAt);
      return new EstimationDto(duration, pickupAt, pickupEndAt, completedAt);
    } catch (Exception ex) {
      log.error("Failed to estimate full trip: pickup={}, dropOff={}, current={}, from={}", pickup, dropOff, current, from, ex);
      throw ex;
    }
  }

  @Override
  public EstimationDto estimate(Location pickup, Location dropOff, LocalDateTime pickupAt) {
    log.info("Estimating completion: pickup={}, dropOff={}, pickupAt={}", pickup, dropOff, pickupAt);
    try {
      int estimatedDuration = geoService.estimateDuration(pickup, dropOff);
      LocalDateTime estimatedCompletedAt = pickupAt.plusMinutes(estimatedDuration);
      log.info("Estimation result: estimatedDuration={}, estimatedCompletedAt={}", estimatedDuration, estimatedCompletedAt);
      return new EstimationDto(estimatedDuration, null, null, estimatedCompletedAt);
    } catch (Exception ex) {
      log.error("Failed to estimate completion: pickup={}, dropOff={}, pickupAt={}", pickup, dropOff, pickupAt, ex);
      throw ex;
    }
  }

}
