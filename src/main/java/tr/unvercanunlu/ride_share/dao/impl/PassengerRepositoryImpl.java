package tr.unvercanunlu.ride_share.dao.impl;

import java.util.UUID;
import tr.unvercanunlu.ride_share.core.dao.impl.InMemoryDaoImpl;
import tr.unvercanunlu.ride_share.core.log.Logger;
import tr.unvercanunlu.ride_share.dao.PassengerRepository;
import tr.unvercanunlu.ride_share.entity.Passenger;
import tr.unvercanunlu.ride_share.helper.LoggerFactory;

public class PassengerRepositoryImpl extends InMemoryDaoImpl<Passenger> implements PassengerRepository {

  private static final Logger logger = LoggerFactory.getLogger(PassengerRepositoryImpl.class);

  @Override
  public boolean existsById(UUID passengerId) {
    boolean exists = (passengerId != null) && entities.containsKey(passengerId);
    logger.debug("Checked existence for passengerId=%s. Exists=%b".formatted(passengerId, exists));
    return exists;
  }

}
