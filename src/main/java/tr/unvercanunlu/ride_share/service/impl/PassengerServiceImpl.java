package tr.unvercanunlu.ride_share.service.impl;

import java.util.UUID;
import tr.unvercanunlu.ride_share.dao.PassengerRepository;
import tr.unvercanunlu.ride_share.dto.request.RegisterPassengerDto;
import tr.unvercanunlu.ride_share.entity.Passenger;
import tr.unvercanunlu.ride_share.exception.PassengerNotFoundException;
import tr.unvercanunlu.ride_share.service.PassengerService;
import tr.unvercanunlu.ride_share.service.ValidationService;

public class PassengerServiceImpl implements PassengerService {

  private final PassengerRepository passengerRepository;
  private final ValidationService validationService;

  public PassengerServiceImpl(PassengerRepository passengerRepository, ValidationService validationService) {
    this.passengerRepository = passengerRepository;
    this.validationService = validationService;
  }

  @Override
  public Passenger register(RegisterPassengerDto request) {
    Passenger passenger = Passenger.of(request);
    return passengerRepository.save(passenger);
  }

  @Override
  public Passenger getDetail(UUID passengerId) throws PassengerNotFoundException {
    validationService.checkIdentifier(passengerId, Passenger.class);
    return passengerRepository.get(passengerId).orElseThrow(() -> new PassengerNotFoundException(passengerId));
  }

}
