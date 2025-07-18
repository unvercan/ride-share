package tr.unvercanunlu.ride_share.dao.impl;

import java.util.UUID;
import tr.unvercanunlu.ride_share.core.impl.InMemoryDaoImpl;
import tr.unvercanunlu.ride_share.dao.PassengerRepository;
import tr.unvercanunlu.ride_share.entity.Passenger;

public class PassengerRepositoryImpl extends InMemoryDaoImpl<Passenger> implements PassengerRepository {

  @Override
  public boolean checkExists(UUID passengerId) {
    return entities.values()
        .stream()
        .anyMatch(passenger -> passenger.getId().equals(passengerId));
  }

}
