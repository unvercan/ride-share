package tr.unvercanunlu.ride_share.core.impl;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import tr.unvercanunlu.ride_share.core.BaseEntity;
import tr.unvercanunlu.ride_share.core.Dao;

public abstract class InMemoryDaoImpl<T extends BaseEntity<UUID>> implements Dao<T, UUID> {

  protected final ConcurrentMap<UUID, T> entities = new ConcurrentHashMap<>();

  @Override
  public Optional<T> get(UUID id) {
    return Optional.ofNullable(
        entities.get(id)
    );
  }

  @Override
  public T save(T data) {
    if (data.getId() == null) {
      data.setId(UUID.randomUUID());
    }

    entities.put(data.getId(), data);

    return data;
  }

  @Override
  public void remove(UUID id) {
    entities.remove(id);
  }

}
