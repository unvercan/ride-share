package tr.unvercanunlu.ride_share.service.impl;

import java.util.Random;
import tr.unvercanunlu.ride_share.entity.Location;
import tr.unvercanunlu.ride_share.service.IMapService;

public class MapService implements IMapService {

  @Override
  public double calculateDistance(Location start, Location end) {
    return new Random(System.currentTimeMillis()).nextInt(3, 50);
  }

  @Override
  public int estimateDuration(Location start, Location end) {
    return new Random(System.currentTimeMillis()).nextInt(3, 50);
  }

}
