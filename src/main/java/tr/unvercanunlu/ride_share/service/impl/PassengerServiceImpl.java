package tr.unvercanunlu.ride_share.service.impl;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import tr.unvercanunlu.ride_share.core.log.Logger;
import tr.unvercanunlu.ride_share.dao.PassengerRepository;
import tr.unvercanunlu.ride_share.dto.request.RegisterPassengerDto;
import tr.unvercanunlu.ride_share.entity.EntityFactory;
import tr.unvercanunlu.ride_share.entity.Passenger;
import tr.unvercanunlu.ride_share.exception.NotFoundException;
import tr.unvercanunlu.ride_share.helper.LoggerFactory;
import tr.unvercanunlu.ride_share.service.PassengerService;
import tr.unvercanunlu.ride_share.service.ValidationService;

@RequiredArgsConstructor
public class PassengerServiceImpl implements PassengerService {

  private static final Logger logger = LoggerFactory.getLogger(PassengerServiceImpl.class);

  private final PassengerRepository passengerRepository;
  private final ValidationService validationService;

  @Override
  public Passenger register(RegisterPassengerDto request) {
    try {
      logger.info("Registering passenger: email=%s".formatted(request.email()));
      Passenger passenger = EntityFactory.from(request);
      passenger = passengerRepository.save(passenger);
      logger.info("Passenger registered: email=%s id=%s".formatted(request.email(), passenger.getId()));
      return passenger;
    } catch (Exception e) {
      logger.error("Failed to register passenger: error=%s".formatted(e.getMessage()), e);
      throw e;
    }
  }

  @Override
  public Passenger getPassenger(UUID passengerId) throws NotFoundException {
    try {
      logger.info("Retrieving passenger detail: passengerId=%s".formatted(passengerId));
      validationService.checkIdentifier(passengerId, Passenger.class);
      Passenger passenger = passengerRepository.findById(passengerId).orElseThrow(() -> new NotFoundException(Passenger.class, passengerId));
      logger.info("Passenger detail retrieved: passengerId=%s".formatted(passengerId));
      return passenger;
    } catch (Exception e) {
      logger.error("Failed to retrieve passenger detail: passengerId=%s error=%s".formatted(passengerId, e.getMessage()), e);
      throw e;
    }
  }

}
