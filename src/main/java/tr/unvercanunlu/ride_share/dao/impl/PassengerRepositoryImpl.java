package tr.unvercanunlu.ride_share.dao.impl;

import java.util.UUID;
import tr.unvercanunlu.ride_share.core.impl.InMemoryDaoImpl;
import tr.unvercanunlu.ride_share.dao.PassengerRepository;
import tr.unvercanunlu.ride_share.entity.Passenger;
import tr.unvercanunlu.ride_share.helper.LogHelper;

public class PassengerRepositoryImpl extends InMemoryDaoImpl<Passenger> implements PassengerRepository {

  @Override
  public boolean checkExists(UUID passengerId) {
    boolean exists = (passengerId != null) && entities.containsKey(passengerId);

    LogHelper.debug(this.getClass(),
        String.format("Checked existence for passengerId=%s. Exists=%b", passengerId, exists));

    return exists;
  }

}
