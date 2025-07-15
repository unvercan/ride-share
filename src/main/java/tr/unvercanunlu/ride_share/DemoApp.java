package tr.unvercanunlu.ride_share;

import java.util.List;
import java.util.UUID;
import tr.unvercanunlu.ride_share.dao.DriverRepository;
import tr.unvercanunlu.ride_share.dao.PassengerRepository;
import tr.unvercanunlu.ride_share.dao.RideRepository;
import tr.unvercanunlu.ride_share.dao.impl.DriverRepositoryImpl;
import tr.unvercanunlu.ride_share.dao.impl.PassengerRepositoryImpl;
import tr.unvercanunlu.ride_share.dao.impl.RideRepositoryImpl;
import tr.unvercanunlu.ride_share.dto.NearRequestedRideDto;
import tr.unvercanunlu.ride_share.dto.request.AcceptRideDto;
import tr.unvercanunlu.ride_share.dto.request.RegisterDriverDto;
import tr.unvercanunlu.ride_share.dto.request.RegisterPassengerDto;
import tr.unvercanunlu.ride_share.dto.request.RequestRideDto;
import tr.unvercanunlu.ride_share.dto.response.PassengerPickupDto;
import tr.unvercanunlu.ride_share.dto.response.RideAcceptedDto;
import tr.unvercanunlu.ride_share.dto.response.RideCanceledDto;
import tr.unvercanunlu.ride_share.dto.response.RideCompletedDto;
import tr.unvercanunlu.ride_share.dto.response.RideRequestedDto;
import tr.unvercanunlu.ride_share.entity.Driver;
import tr.unvercanunlu.ride_share.entity.Location;
import tr.unvercanunlu.ride_share.entity.Passenger;
import tr.unvercanunlu.ride_share.entity.Ride;
import tr.unvercanunlu.ride_share.exception.DriverHasActiveRideException;
import tr.unvercanunlu.ride_share.exception.DriverMissingException;
import tr.unvercanunlu.ride_share.exception.DriverNotFoundException;
import tr.unvercanunlu.ride_share.exception.DriverUnavailableException;
import tr.unvercanunlu.ride_share.exception.PassengerHasActiveRideException;
import tr.unvercanunlu.ride_share.exception.PassengerNotFoundException;
import tr.unvercanunlu.ride_share.exception.RideNotFoundException;
import tr.unvercanunlu.ride_share.service.DriverService;
import tr.unvercanunlu.ride_share.service.MapService;
import tr.unvercanunlu.ride_share.service.PassengerService;
import tr.unvercanunlu.ride_share.service.RideService;
import tr.unvercanunlu.ride_share.service.impl.DriverServiceImpl;
import tr.unvercanunlu.ride_share.service.impl.MapServiceImpl;
import tr.unvercanunlu.ride_share.service.impl.PassengerServiceImpl;
import tr.unvercanunlu.ride_share.service.impl.RideServiceImpl;

public class DemoApp {

  public static void main(String[] args) {
    // Repositories
    DriverRepository driverRepository = new DriverRepositoryImpl();
    PassengerRepository passengerRepository = new PassengerRepositoryImpl();
    RideRepository rideRepository = new RideRepositoryImpl();

    // Services
    MapService mapService = new MapServiceImpl();
    DriverService driverService = new DriverServiceImpl(driverRepository, rideRepository);
    PassengerService passengerService = new PassengerServiceImpl(passengerRepository);
    RideService rideService = new RideServiceImpl(rideRepository, driverRepository, passengerRepository, mapService);

    // === DRIVER SERVICE ===
    System.out.println("=== DRIVER SERVICE DEMO ===");

    // Register Driver
    RegisterDriverDto registerDriverDto = new RegisterDriverDto("Alice", "alice@mail.com", "123456", "AAA-111");
    Driver driver = driverService.register(registerDriverDto);
    System.out.println("Registered driver: " + driver.getId());

    // Update Location (valid)
    Location current = new Location(10.0, 20.0);
    driverService.updateLocation(driver.getId(), current);

    // Make driver available
    driverService.makeAvailable(driver.getId());
    System.out.println("Driver status after makeAvailable: " + driverService.getDetail(driver.getId()).getStatus());

    // Try to update location for non-existent driver
    try {
      driverService.updateLocation(UUID.randomUUID(), current);
    } catch (DriverNotFoundException ex) {
      System.out.println("Caught expected: " + ex.getMessage());
    }

    // Try to make offline when no active ride (should succeed)
    driverService.makeOffline(driver.getId());
    System.out.println("Driver status after makeOffline: " + driverService.getDetail(driver.getId()).getStatus());

    // Try to get driver detail for fake id
    try {
      driverService.getDetail(UUID.randomUUID());
    } catch (DriverNotFoundException ex) {
      System.out.println("Caught expected: " + ex.getMessage());
    }

    // === PASSENGER SERVICE ===
    System.out.println("\n=== PASSENGER SERVICE DEMO ===");

    RegisterPassengerDto registerPassengerDto = new RegisterPassengerDto("Bob", "bob@mail.com", "654321");
    Passenger passenger = passengerService.register(registerPassengerDto);
    System.out.println("Registered passenger: " + passenger.getId());

    // Get detail
    Passenger fetched = passengerService.getDetail(passenger.getId());
    System.out.println("Fetched passenger: " + fetched.getName());

    // Try to get detail for non-existent passenger
    try {
      passengerService.getDetail(UUID.randomUUID());
    } catch (PassengerNotFoundException ex) {
      System.out.println("Caught expected: " + ex.getMessage());
    }

    // === RIDE SERVICE ===
    System.out.println("\n=== RIDE SERVICE DEMO ===");

    // Make driver available for ride again
    driverService.makeAvailable(driver.getId());

    // Prepare pickup/dropoff
    Location pickup = new Location(10.1, 20.1);
    Location dropOff = new Location(10.5, 20.7);

    // Passenger requests a ride
    RequestRideDto requestRideDto = new RequestRideDto(passenger.getId(), pickup, dropOff);
    RideRequestedDto requested = rideService.request(requestRideDto);
    System.out.println("Ride requested: " + requested.id());

    // Try to request another ride for same passenger (should throw PassengerHasActiveRideException)
    try {
      RequestRideDto requestRideDtoForPassengerActiveRide = new RequestRideDto(passenger.getId(), pickup, dropOff);
      rideService.request(requestRideDtoForPassengerActiveRide);
    } catch (PassengerHasActiveRideException ex) {
      System.out.println("Caught expected: " + ex.getMessage());
    }

    // Find nearest requested rides (should include our ride)
    List<NearRequestedRideDto> nearby = rideService.findNearestRequestedRides(current);
    System.out.println("Nearby requested rides: " + nearby.size());

    // Accept the ride
    AcceptRideDto acceptDto = new AcceptRideDto(requested.id(), driver.getId(), current);
    RideAcceptedDto accepted = rideService.accept(acceptDto);
    System.out.println("Ride accepted by driver: " + accepted.driverId());

    // Try to accept ride with unavailable driver (should throw DriverUnavailableException)
    try {
      // Driver is already BUSY after accepting above!
      AcceptRideDto acceptRideDtoForDriverUnavailable = new AcceptRideDto(requested.id(), driver.getId(), current);
      rideService.accept(acceptRideDtoForDriverUnavailable);
    } catch (DriverUnavailableException ex) {
      System.out.println("Caught expected: " + ex.getMessage());
    }

    // Try to accept with random driver (should throw DriverNotFoundException)
    try {
      AcceptRideDto acceptRideDtoForDriverNotFound = new AcceptRideDto(requested.id(), UUID.randomUUID(), current);
      rideService.accept(acceptRideDtoForDriverNotFound);
    } catch (DriverNotFoundException ex) {
      System.out.println("Caught expected: " + ex.getMessage());
    }

    // Pickup passenger
    PassengerPickupDto pickupDto = rideService.pickupPassenger(requested.id());
    System.out.println("Passenger picked up at: " + pickupDto.pickupAt());

    // Try to pickup a non-existent ride
    try {
      rideService.pickupPassenger(UUID.randomUUID());
    } catch (RideNotFoundException ex) {
      System.out.println("Caught expected: " + ex.getMessage());
    }

    // Complete the ride
    RideCompletedDto completedDto = rideService.complete(requested.id());
    System.out.println("Ride completed at: " + completedDto.completedAt());

    // Try to complete ride with missing driver (simulate by manually setting driverId to null)
    Ride ride = rideService.getDetail(requested.id());
    ride.setDriverId(null);
    rideRepository.save(ride); // Save the change

    try {
      rideService.complete(requested.id());
    } catch (DriverMissingException ex) {
      System.out.println("Caught expected: " + ex.getMessage());
    }

    // Cancel a ride (first, create new ride)
    driverService.makeAvailable(driver.getId());
    RequestRideDto requestRideDtoToCancel = new RequestRideDto(passenger.getId(), pickup, dropOff);
    RideRequestedDto toCancel = rideService.request(requestRideDtoToCancel);
    AcceptRideDto acceptToCancel = new AcceptRideDto(toCancel.id(), driver.getId(), current);
    rideService.accept(acceptToCancel);

    RideCanceledDto canceled = rideService.cancel(toCancel.id());
    System.out.println("Ride canceled at: " + canceled.canceledAt());

    // Try to cancel a non-existent ride
    try {
      rideService.cancel(UUID.randomUUID());
    } catch (RideNotFoundException ex) {
      System.out.println("Caught expected: " + ex.getMessage());
    }

    // Get ride detail (valid/invalid)
    Ride detail = rideService.getDetail(toCancel.id());
    System.out.println("Canceled ride status: " + detail.getStatus());
    try {
      rideService.getDetail(UUID.randomUUID());
    } catch (RideNotFoundException ex) {
      System.out.println("Caught expected: " + ex.getMessage());
    }

    // Get ride histories
    List<Ride> passengerHistory = rideService.getHistoryOfPassenger(passenger.getId());
    System.out.println("Passenger ride history count: " + passengerHistory.size());

    List<Ride> driverHistory = rideService.getHistoryOfDriver(driver.getId());
    System.out.println("Driver ride history count: " + driverHistory.size());

    // Try histories for non-existent driver/passenger
    try {
      rideService.getHistoryOfPassenger(UUID.randomUUID());
    } catch (PassengerNotFoundException ex) {
      System.out.println("Caught expected: " + ex.getMessage());
    }
    try {
      rideService.getHistoryOfDriver(UUID.randomUUID());
    } catch (DriverNotFoundException ex) {
      System.out.println("Caught expected: " + ex.getMessage());
    }

    // Try to make offline while driver has active ride (should throw)
    driverService.makeAvailable(driver.getId());
    RequestRideDto requestRideDtoForActiveRideOfDriver = new RequestRideDto(passenger.getId(), pickup, dropOff);
    RideRequestedDto rideForActive = rideService.request(requestRideDtoForActiveRideOfDriver);
    AcceptRideDto acceptRideDtoForActiveRideOfDriver = new AcceptRideDto(rideForActive.id(), driver.getId(), current);
    rideService.accept(acceptRideDtoForActiveRideOfDriver);
    try {
      driverService.makeOffline(driver.getId());
    } catch (DriverHasActiveRideException ex) {
      System.out.println("Caught expected: " + ex.getMessage());
    }
  }

}
