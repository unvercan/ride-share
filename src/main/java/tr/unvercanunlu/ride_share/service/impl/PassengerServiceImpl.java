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
      logger.info(String.format("Registering passenger: email=%s", request.email()));
      Passenger passenger = EntityFactory.from(request);
      passenger = passengerRepository.save(passenger);
      logger.info(String.format("Passenger registered: email=%s id=%s", request.email(), passenger.getId()));
      return passenger;
    } catch (Exception e) {
      logger.error(String.format("Failed to register passenger: error=%s", e.getMessage()), e);
      throw e;
    }
  }

  @Override
  public Passenger getPassenger(UUID passengerId) throws NotFoundException {
    try {
      logger.info(String.format("Retrieving passenger detail: passengerId=%s", passengerId));
      validationService.checkIdentifier(passengerId, Passenger.class);
      Passenger passenger = passengerRepository.findById(passengerId).orElseThrow(() -> new NotFoundException(Passenger.class, passengerId));
      logger.info(String.format("Passenger detail retrieved: passengerId=%s", passengerId));
      return passenger;
    } catch (Exception e) {
      logger.error(String.format("Failed to retrieve passenger detail: passengerId=%s error=%s", passengerId, e.getMessage()), e);
      throw e;
    }
  }

}
