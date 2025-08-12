package tr.unvercanunlu.ride_share.service.impl;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import tr.unvercanunlu.ride_share.core.log.Logger;
import tr.unvercanunlu.ride_share.entity.Location;
import tr.unvercanunlu.ride_share.helper.LoggerFactory;
import tr.unvercanunlu.ride_share.service.GeoService;

public class RandomGeoServiceImpl implements GeoService {

  private static final Logger logger = LoggerFactory.getLogger(RandomGeoServiceImpl.class);

  @Override
  public double calculateDistance(Location start, Location end) {
    logger.info("Calculating distance: start=%s, end=%s".formatted(start, end));

    try {
      double distance = getRandom().nextInt(3, 50);
      logger.info("Distance calculated: start=%s, end=%s, distance=%.2f".formatted(start, end, distance));
      return distance;
    } catch (Exception e) {
      logger.error("Failed to calculate distance: start=%s, end=%s, error=%s".formatted(start, end, e.getMessage()), e);
      throw e;
    }
  }

  @Override
  public int estimateDuration(Location start, Location end) {
    logger.info("Estimating duration: start=%s, end=%s".formatted(start, end));

    try {
      int duration = getRandom().nextInt(3, 50);
      logger.info("Duration estimated: start=%s, end=%s, duration=%d".formatted(start, end, duration));
      return duration;
    } catch (Exception e) {
      logger.error("Failed to estimate duration: start=%s, end=%s, error=%s".formatted(start, end, e.getMessage()), e);
      throw e;
    }
  }

  private static Random getRandom() {
    return ThreadLocalRandom.current();
  }

}
