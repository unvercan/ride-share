package tr.unvercanunlu.ride_share.service.impl;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import tr.unvercanunlu.ride_share.entity.Location;
import tr.unvercanunlu.ride_share.service.GeoService;

public class GeoServiceImpl implements GeoService {

  @Override
  public double calculateDistance(Location start, Location end) {
    return getRandom().nextInt(3, 50);
  }

  @Override
  public int estimateDuration(Location start, Location end) {
    return getRandom().nextInt(3, 50);
  }

  private static Random getRandom() {
    return ThreadLocalRandom.current();
  }

}
