package tr.unvercanunlu.ride_share;

import java.util.List;
import tr.unvercanunlu.ride_share.dao.RideRepository;
import tr.unvercanunlu.ride_share.dao.impl.DriverRepositoryImpl;
import tr.unvercanunlu.ride_share.dao.impl.PassengerRepositoryImpl;
import tr.unvercanunlu.ride_share.dao.impl.RideRepositoryImpl;
import tr.unvercanunlu.ride_share.dto.request.AcceptRideDto;
import tr.unvercanunlu.ride_share.dto.request.RegisterDriverDto;
import tr.unvercanunlu.ride_share.dto.request.RegisterPassengerDto;
import tr.unvercanunlu.ride_share.dto.request.RequestRideDto;
import tr.unvercanunlu.ride_share.dto.request.UpdateLocationDto;
import tr.unvercanunlu.ride_share.entity.Location;
import tr.unvercanunlu.ride_share.service.CalculationService;
import tr.unvercanunlu.ride_share.service.DriverService;
import tr.unvercanunlu.ride_share.service.EstimationService;
import tr.unvercanunlu.ride_share.service.GeoService;
import tr.unvercanunlu.ride_share.service.PassengerService;
import tr.unvercanunlu.ride_share.service.RideService;
import tr.unvercanunlu.ride_share.service.ValidationService;
import tr.unvercanunlu.ride_share.service.impl.CalculationServiceImpl;
import tr.unvercanunlu.ride_share.service.impl.DriverServiceImpl;
import tr.unvercanunlu.ride_share.service.impl.EstimationServiceImpl;
import tr.unvercanunlu.ride_share.service.impl.PassengerServiceImpl;
import tr.unvercanunlu.ride_share.service.impl.RandomGeoServiceImpl;
import tr.unvercanunlu.ride_share.service.impl.RideServiceImpl;
import tr.unvercanunlu.ride_share.service.impl.ValidationServiceImpl;

public class App {

  public static void main(String[] args) {
    // --- Setup in-memory repositories ---
    DriverRepositoryImpl driverRepo = new DriverRepositoryImpl();
    PassengerRepositoryImpl passengerRepo = new PassengerRepositoryImpl();
    RideRepository rideRepo = new RideRepositoryImpl(); // implement using InMemoryDaoImpl<Ride>

    // --- Setup services ---
    GeoService geoService = new RandomGeoServiceImpl();
    CalculationService calcService = new CalculationServiceImpl();
    EstimationService estimationService = new EstimationServiceImpl(geoService);
    ValidationService validationService = new ValidationServiceImpl(rideRepo, driverRepo, passengerRepo);
    DriverService driverService = new DriverServiceImpl(driverRepo, validationService);
    PassengerService passengerService = new PassengerServiceImpl(passengerRepo, validationService);
    RideService rideService = new RideServiceImpl(rideRepo, driverRepo, geoService, calcService, estimationService, validationService);

    // --- 1. Register passenger ---
    var passenger = passengerService.register(new RegisterPassengerDto(
        "John Doe", "john@example.com", "+12345678"
    ));
    System.out.println("Registered Passenger: " + passenger);

    // --- 2. Register driver ---
    var driver = driverService.register(new RegisterDriverDto(
        "Alice Driver", "alice@example.com", "+987654321", "XYZ-1234"
    ));
    System.out.println("Registered Driver: " + driver);

    // --- 3. Update driver location & set available ---
    driverService.updateLocation(new UpdateLocationDto(driver.getId(), new Location(40.0, 29.0)));
    driverService.setAvailable(driver.getId());

    // --- 4. Passenger requests a ride ---
    var rideRequest = rideService.requestRide(new RequestRideDto(
        passenger.getId(),
        new Location(40.0, 29.0),   // pickup
        new Location(40.2, 29.1)    // drop-off
    ));
    System.out.println("Ride Requested: " + rideRequest);

    // --- 5. Driver finds nearest rides ---
    List<?> nearbyRides = rideService.findNearestRequestedRides(new Location(40.0, 29.0));
    System.out.println("Nearby Rides: " + nearbyRides);

    // --- 6. Driver accepts ride ---
    var acceptedRide = rideService.acceptRide(new AcceptRideDto(
        rideRequest.id(),
        driver.getId(),
        new Location(40.0, 29.0)
    ));
    System.out.println("Ride Accepted: " + acceptedRide);

    // --- 7. Passenger approves driver ---
    var approvedRide = rideService.approveAssignedDriver(acceptedRide.id());
    System.out.println("Ride Approved: " + approvedRide);

    // --- 8. Driver starts trip ---
    var pickupDto = rideService.startTrip(approvedRide.id());
    System.out.println("Trip Started: " + pickupDto);

    // --- 9. Complete trip ---
    var completedRide = rideService.completeTrip(approvedRide.id());
    System.out.println("Trip Completed: " + completedRide);
  }
}
