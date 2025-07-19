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
    return Optional.ofNullable(id)
        .map(entities::get);
  }

  @Override
  public List<T> getAll() {
    return new ArrayList<>(entities.values());
  }

  @Override
  public T save(T entity) {
    if (entity == null) {
      return null;
    }

    ensureId(entity);
    entities.put(entity.getId(), entity);
    return entity;
  }

  @Override
  public void remove(UUID id) {
    if (id != null) {
      entities.remove(id);
    }
  }

  private void ensureId(T entity) {
    if ((entity != null) && (entity.getId() == null)) {
      UUID id = UUID.randomUUID();
      entity.setId(id);
    }
  }

}
