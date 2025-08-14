package tr.unvercanunlu.ride_share.entity;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tr.unvercanunlu.ride_share.dto.request.RegisterDriverDto;
import tr.unvercanunlu.ride_share.dto.request.RegisterPassengerDto;
import tr.unvercanunlu.ride_share.dto.request.RequestRideDto;
import tr.unvercanunlu.ride_share.status.DriverStatus;
import tr.unvercanunlu.ride_share.status.RideStatus;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EntityFactory {

  public static Driver from(RegisterDriverDto request) {
    if (request == null) {
      log.error("RegisterDriverDto is null!");
      throw new IllegalArgumentException("RegisterDriverDto cannot be null");
    }

    Driver driver = new Driver();
    driver.setName(request.name());
    driver.setEmail(request.email());
    driver.setPhone(request.phone());
    driver.setPlate(request.plate());
    driver.setStatus(DriverStatus.OFFLINE);

    log.info("New Driver entity created: driver={}", driver);
    return driver;
  }

  public static Passenger from(RegisterPassengerDto request) {
    if (request == null) {
      log.error("RegisterPassengerDto is null!");
      throw new IllegalArgumentException("RegisterPassengerDto cannot be null");
    }

    Passenger passenger = new Passenger();
    passenger.setName(request.name());
    passenger.setEmail(request.email());
    passenger.setPhone(request.phone());

    log.info("New Passenger entity created: passenger={}", passenger);
    return passenger;
  }

  public static Ride from(RequestRideDto request) {
    if (request == null) {
      log.error("RequestRideDto is null!");
      throw new IllegalArgumentException("RequestRideDto cannot be null");
    }

    Ride ride = new Ride();
    ride.setPassengerId(request.passengerId());
    ride.setPickup(request.pickup());
    ride.setDropOff(request.dropOff());
    ride.setStatus(RideStatus.REQUESTED);

    log.info("New Ride entity created: ride={}", ride);
    return ride;
  }

}
