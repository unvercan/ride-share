package tr.unvercanunlu.ride_share.service.impl;

import java.time.LocalDateTime;
import tr.unvercanunlu.ride_share.config.AppConfig;
import tr.unvercanunlu.ride_share.dto.response.Estimation;
import tr.unvercanunlu.ride_share.entity.Location;
import tr.unvercanunlu.ride_share.service.EstimationService;
import tr.unvercanunlu.ride_share.service.GeoService;

public class EstimationServiceImpl implements EstimationService {

  private final GeoService geoService;

  public EstimationServiceImpl(GeoService geoService) {
    this.geoService = geoService;
  }

  @Override
  public Estimation estimate(Location pickup, Location dropOff) {
    int duration = geoService.estimateDuration(pickup, dropOff);
    return new Estimation(duration, null, null, null);
  }

  @Override
  public Estimation estimate(Location pickup, Location dropOff, Location current, LocalDateTime from) {
    int duration = geoService.estimateDuration(pickup, dropOff);
    int durationToPickup = geoService.estimateDuration(current, pickup);
    LocalDateTime pickupAt = from.plusMinutes(durationToPickup);
    LocalDateTime pickupEndAt = pickupAt.plusMinutes(AppConfig.MAX_DURATION);
    LocalDateTime completedAt = pickupAt.plusMinutes(duration);
    return new Estimation(duration, pickupAt, pickupEndAt, completedAt);
  }

  @Override
  public Estimation estimate(Location pickup, Location dropOff, LocalDateTime pickupAt) {
    int estimatedDuration = geoService.estimateDuration(pickup, dropOff);
    LocalDateTime estimatedCompletedAt = pickupAt.plusMinutes(estimatedDuration);
    return new Estimation(estimatedDuration, null, null, estimatedCompletedAt);
  }

}
