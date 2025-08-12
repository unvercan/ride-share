package tr.unvercanunlu.ride_share.service;

import java.time.LocalDateTime;
import tr.unvercanunlu.ride_share.dto.response.EstimationDto;
import tr.unvercanunlu.ride_share.entity.Location;

public interface EstimationService {

  EstimationDto estimate(Location pickup, Location dropOff);

  EstimationDto estimate(Location pickup, Location dropOff, Location current, LocalDateTime from);

  EstimationDto estimate(Location pickup, Location dropOff, LocalDateTime pickupAt);

}
