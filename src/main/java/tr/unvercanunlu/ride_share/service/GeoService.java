package tr.unvercanunlu.ride_share.service;

import tr.unvercanunlu.ride_share.entity.Location;

public interface GeoService {

  double calculateDistance(Location start, Location end);

  int estimateDuration(Location start, Location end);

}
