package tr.unvercanunlu.ride_share.service;

import java.util.UUID;
import tr.unvercanunlu.ride_share.dto.request.RegisterDriverDto;
import tr.unvercanunlu.ride_share.entity.Driver;
import tr.unvercanunlu.ride_share.entity.Location;
import tr.unvercanunlu.ride_share.exception.DriverNotFoundException;

public interface IDriverService {

  Driver registerDriver(RegisterDriverDto request);

  Driver updateLocation(UUID driverId, Location current) throws DriverNotFoundException;

  Driver makeOffline(UUID driverId) throws DriverNotFoundException;

  Driver makeAvailable(UUID driverId) throws DriverNotFoundException;

  Driver getDriverDetail(UUID driverId) throws DriverNotFoundException;

}
