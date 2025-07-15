package tr.unvercanunlu.ride_share.core.impl;

import java.util.ArrayList;
import java.util.List;
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
  public List<T> getAll() {
    return new ArrayList<>(entities.values());
  }

  @Override
  public T save(T entity) {
    ensureId(entity);

    entities.put(entity.getId(), entity);

    return entity;
  }

  @Override
  public void remove(UUID id) {
    entities.remove(id);
  }

  protected void ensureId(T entity) {
    if (entity.getId() == null) {
      entity.setId(UUID.randomUUID());
    }
  }

}
