package tr.unvercanunlu.ride_share.service.impl;

import java.util.Optional;
import java.util.UUID;
import tr.unvercanunlu.ride_share.dao.IPassengerDao;
import tr.unvercanunlu.ride_share.dao.impl.PassengerDao;
import tr.unvercanunlu.ride_share.dto.request.RegisterPassengerDto;
import tr.unvercanunlu.ride_share.entity.Passenger;
import tr.unvercanunlu.ride_share.exception.PassengerNotFoundException;
import tr.unvercanunlu.ride_share.service.IPassengerService;

public class PassengerService implements IPassengerService {

  private final IPassengerDao passengerDao = new PassengerDao();

  @Override
  public Passenger register(RegisterPassengerDto request) {
    Passenger passenger = new Passenger();
    passenger.setId(UUID.randomUUID());
    passenger.setName(request.name());
    passenger.setEmail(request.email());
    passenger.setPhone(request.phone());

    return passengerDao.save(passenger);
  }

  @Override
  public Passenger getDetail(UUID passengerId) throws PassengerNotFoundException {
    return Optional.ofNullable(
        passengerDao.get(passengerId)
    ).orElseThrow(() -> new PassengerNotFoundException(passengerId));
  }

}
