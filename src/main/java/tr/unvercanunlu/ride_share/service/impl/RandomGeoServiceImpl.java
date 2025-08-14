package tr.unvercanunlu.ride_share.service.impl;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import tr.unvercanunlu.ride_share.entity.Location;
import tr.unvercanunlu.ride_share.service.GeoService;

@Slf4j
public class RandomGeoServiceImpl implements GeoService {

  private static Random getRandom() {
    return ThreadLocalRandom.current();
  }

  @Override
  public double calculateDistance(Location start, Location end) {
    log.info("Calculating distance: start={}, end={}", start, end);

    try {
      double distance = getRandom().nextInt(3, 50);
      log.info("Distance calculated: start={}, end={}, distance={}", start, end, distance);
      return distance;
    } catch (Exception e) {
      log.error("Failed to calculate distance: start={}, end={}, error={}", start, end, e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public int estimateDuration(Location start, Location end) {
    log.info("Estimating duration: start={}, end={}", start, end);

    try {
      int duration = getRandom().nextInt(3, 50);
      log.info("Duration estimated: start={}, end={}, duration={}", start, end, duration);
      return duration;
    } catch (Exception e) {
      log.error("Failed to estimate duration: start={}, end={}, error={}", start, end, e.getMessage(), e);
      throw e;
    }
  }

}
