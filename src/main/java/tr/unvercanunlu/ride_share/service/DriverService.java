package tr.unvercanunlu.ride_share.service;

import java.util.UUID;
import tr.unvercanunlu.ride_share.dto.request.RegisterDriverDto;
import tr.unvercanunlu.ride_share.dto.request.UpdateLocationDto;
import tr.unvercanunlu.ride_share.entity.Driver;
import tr.unvercanunlu.ride_share.exception.DriverNotFoundException;

public interface DriverService {

  Driver register(RegisterDriverDto request);

  Driver updateLocation(UpdateLocationDto request);

  Driver makeOffline(UUID driverId);

  Driver makeAvailable(UUID driverId);

  Driver getDetail(UUID driverId) throws DriverNotFoundException;

}
