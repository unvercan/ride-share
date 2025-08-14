package tr.unvercanunlu.ride_share.dao.impl;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import tr.unvercanunlu.ride_share.core.dao.impl.InMemoryDaoImpl;
import tr.unvercanunlu.ride_share.dao.PassengerRepository;
import tr.unvercanunlu.ride_share.entity.Passenger;
import tr.unvercanunlu.ride_share.util.ValidationUtil;

@Slf4j
public class PassengerRepositoryImpl extends InMemoryDaoImpl<Passenger> implements PassengerRepository {

  @Override
  public boolean existsById(UUID passengerId) {
    ValidationUtil.checkIdNotNull(passengerId);
    boolean exists = entities.containsKey(passengerId);
    log.debug("Checked existence for passengerId={}. Exists={}", passengerId, exists);
    return exists;
  }

}
