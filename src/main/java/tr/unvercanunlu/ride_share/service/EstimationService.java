package tr.unvercanunlu.ride_share.service;

import java.time.LocalDateTime;
import tr.unvercanunlu.ride_share.dto.response.Estimation;
import tr.unvercanunlu.ride_share.entity.Location;

public interface EstimationService {

  Estimation estimate(Location pickup, Location dropOff);

  Estimation estimate(Location pickup, Location dropOff, Location current, LocalDateTime from);

  Estimation estimate(Location pickup, Location dropOff, LocalDateTime pickupAt);

}
