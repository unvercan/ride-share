package tr.unvercanunlu.ride_share.service.impl;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tr.unvercanunlu.ride_share.dao.PassengerRepository;
import tr.unvercanunlu.ride_share.dto.request.RegisterPassengerDto;
import tr.unvercanunlu.ride_share.entity.EntityFactory;
import tr.unvercanunlu.ride_share.entity.Passenger;
import tr.unvercanunlu.ride_share.exception.NotFoundException;
import tr.unvercanunlu.ride_share.service.PassengerService;
import tr.unvercanunlu.ride_share.service.ValidationService;

@Slf4j
@RequiredArgsConstructor
public class PassengerServiceImpl implements PassengerService {

  private final PassengerRepository passengerRepository;
  private final ValidationService validationService;

  @Override
  public Passenger register(RegisterPassengerDto request) {
    try {
      log.info("Registering passenger: email={}", request.email());
      Passenger passenger = EntityFactory.from(request);
      passenger = passengerRepository.save(passenger);
      log.info("Passenger registered: email={} id={}", request.email(), passenger.getId());
      return passenger;
    } catch (Exception e) {
      log.error("Failed to register passenger: error={}", e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public Passenger getPassenger(UUID passengerId) throws NotFoundException {
    try {
      log.info("Retrieving passenger detail: passengerId={}", passengerId);
      validationService.checkIdentifier(passengerId, Passenger.class);
      Passenger passenger = passengerRepository.findById(passengerId).orElseThrow(() -> new NotFoundException(Passenger.class, passengerId));
      log.info("Passenger detail retrieved: passengerId={}", passengerId);
      return passenger;
    } catch (Exception e) {
      log.error("Failed to retrieve passenger detail: passengerId={} error={}", passengerId, e.getMessage(), e);
      throw e;
    }
  }

}
