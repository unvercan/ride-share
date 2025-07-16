package tr.unvercanunlu.ride_share.service.impl;

import java.util.UUID;
import tr.unvercanunlu.ride_share.dao.PassengerRepository;
import tr.unvercanunlu.ride_share.dto.request.RegisterPassengerDto;
import tr.unvercanunlu.ride_share.entity.Passenger;
import tr.unvercanunlu.ride_share.exception.IdentifierMissingException;
import tr.unvercanunlu.ride_share.exception.PassengerNotFoundException;
import tr.unvercanunlu.ride_share.service.PassengerService;

public class PassengerServiceImpl implements PassengerService {

  private final PassengerRepository passengerRepository;

  public PassengerServiceImpl(PassengerRepository passengerRepository) {
    this.passengerRepository = passengerRepository;
  }

  @Override
  public Passenger register(RegisterPassengerDto request) {
    Passenger passenger = Passenger.of(request);
    return passengerRepository.save(passenger);
  }

  @Override
  public Passenger getDetail(UUID passengerId) throws IdentifierMissingException, PassengerNotFoundException {
    if (passengerId == null) {
      throw new IdentifierMissingException(Passenger.class);
    }

    return passengerRepository.get(passengerId).orElseThrow(() -> new PassengerNotFoundException(passengerId));
  }

}
