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
    logger.info(String.format("Calculating distance: start=%s, end=%s", start, end));

    try {
      double distance = getRandom().nextInt(3, 50);
      logger.info(String.format("Distance calculated: start=%s, end=%s, distance=%.2f", start, end, distance));
      return distance;
    } catch (Exception e) {
      logger.error(String.format("Failed to calculate distance: start=%s, end=%s, error=%s", start, end, e.getMessage()), e);
      throw e;
    }
  }

  @Override
  public int estimateDuration(Location start, Location end) {
    logger.info(String.format("Estimating duration: start=%s, end=%s", start, end));

    try {
      int duration = getRandom().nextInt(3, 50);
      logger.info(String.format("Duration estimated: start=%s, end=%s, duration=%d", start, end, duration));
      return duration;
    } catch (Exception e) {
      logger.error(String.format("Failed to estimate duration: start=%s, end=%s, error=%s", start, end, e.getMessage()), e);
      throw e;
    }
  }

  private static Random getRandom() {
    return ThreadLocalRandom.current();
  }

}
