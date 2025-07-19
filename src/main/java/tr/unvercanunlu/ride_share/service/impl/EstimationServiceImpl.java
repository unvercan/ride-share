package tr.unvercanunlu.ride_share.service.impl;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import tr.unvercanunlu.ride_share.config.AppConfig;
import tr.unvercanunlu.ride_share.dto.response.Estimation;
import tr.unvercanunlu.ride_share.entity.Location;
import tr.unvercanunlu.ride_share.helper.LogHelper;
import tr.unvercanunlu.ride_share.service.EstimationService;
import tr.unvercanunlu.ride_share.service.GeoService;

@RequiredArgsConstructor
public class EstimationServiceImpl implements EstimationService {

  private final GeoService geoService;

  @Override
  public Estimation estimate(Location pickup, Location dropOff) {
    LogHelper.info(this.getClass(),
        String.format("Estimating duration: pickup=%s, dropOff=%s", pickup, dropOff));

    try {
      int duration = geoService.estimateDuration(pickup, dropOff);

      LogHelper.info(this.getClass(),
          String.format("Estimated duration: %d minutes", duration));

      return new Estimation(duration, null, null, null);

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Failed to estimate duration: pickup=%s, dropOff=%s, error=%s", pickup, dropOff, e.getMessage()));

      throw e;
    }
  }

  @Override
  public Estimation estimate(Location pickup, Location dropOff, Location current, LocalDateTime from) {
    LogHelper.info(this.getClass(),
        String.format("Estimating full trip: pickup=%s, dropOff=%s, current=%s, from=%s", pickup, dropOff, current, from));

    try {
      int duration = geoService.estimateDuration(pickup, dropOff);
      int durationToPickup = geoService.estimateDuration(current, pickup);
      LocalDateTime pickupAt = from.plusMinutes(durationToPickup);
      LocalDateTime pickupEndAt = pickupAt.plusMinutes(AppConfig.MAX_DURATION_MINUTES);
      LocalDateTime completedAt = pickupAt.plusMinutes(duration);

      LogHelper.info(this.getClass(),
          String.format("Estimation result: duration=%d, pickupAt=%s, pickupEndAt=%s, completedAt=%s", duration, pickupAt, pickupEndAt, completedAt));

      return new Estimation(duration, pickupAt, pickupEndAt, completedAt);

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Failed to estimate full trip: pickup=%s, dropOff=%s, current=%s, from=%s, error=%s",
              pickup, dropOff, current, from, e.getMessage()));

      throw e;
    }
  }

  @Override
  public Estimation estimate(Location pickup, Location dropOff, LocalDateTime pickupAt) {
    LogHelper.info(this.getClass(),
        String.format("Estimating completion: pickup=%s, dropOff=%s, pickupAt=%s", pickup, dropOff, pickupAt));

    try {
      int estimatedDuration = geoService.estimateDuration(pickup, dropOff);
      LocalDateTime estimatedCompletedAt = pickupAt.plusMinutes(estimatedDuration);

      LogHelper.info(this.getClass(),
          String.format("Estimation result: estimatedDuration=%d, estimatedCompletedAt=%s", estimatedDuration, estimatedCompletedAt));

      return new Estimation(estimatedDuration, null, null, estimatedCompletedAt);

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Failed to estimate completion: pickup=%s, dropOff=%s, pickupAt=%s, error=%s", pickup, dropOff, pickupAt, e.getMessage()));

      throw e;
    }
  }

}
