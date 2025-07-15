package tr.unvercanunlu.ride_share.service.impl;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import tr.unvercanunlu.ride_share.entity.Location;
import tr.unvercanunlu.ride_share.service.MapService;

public class MapServiceImpl implements MapService {

  @Override
  public double calculateDistance(Location start, Location end) {
    return getRandom().nextInt(3, 50);
  }

  @Override
  public int estimateDuration(Location start, Location end) {
    return getRandom().nextInt(3, 50);
  }

  private Random getRandom() {
    return ThreadLocalRandom.current();
  }

}
