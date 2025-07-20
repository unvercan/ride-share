package tr.unvercanunlu.ride_share.core.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import tr.unvercanunlu.ride_share.core.BaseEntity;
import tr.unvercanunlu.ride_share.core.Dao;
import tr.unvercanunlu.ride_share.helper.LogHelper;

public abstract class InMemoryDaoImpl<T extends BaseEntity<UUID>> implements Dao<T, UUID> {

  protected final ConcurrentMap<UUID, T> entities = new ConcurrentHashMap<>();

  @Override
  public Optional<T> get(UUID id) {
    Optional<T> result = Optional.ofNullable(id).map(entities::get);

    if (result.isPresent()) {
      LogHelper.info(this.getClass(),
          String.format("Entity found: id=%s, entity=%s", id, result.get()));

    } else {
      LogHelper.debug(this.getClass(),
          String.format("Entity not found: id=%s", id));
    }
    return result;
  }

  @Override
  public List<T> getAll() {
    LogHelper.debug(this.getClass(),
        String.format("Retrieving all entities. Count=%d", entities.size()));

    return new ArrayList<>(entities.values());
  }

  @Override
  public T save(T entity) {
    if (entity == null) {
      LogHelper.error(this.getClass(), "Attempted to save null entity.");

      return null;
    }

    ensureId(entity);

    entities.put(entity.getId(), entity);

    LogHelper.info(this.getClass(),
        String.format("Entity saved: id=%s, entity=%s", entity.getId(), entity));

    return entity;
  }

  @Override
  public void remove(UUID id) {
    if (id != null) {
      T removed = entities.remove(id);

      if (removed != null) {
        LogHelper.info(this.getClass(),
            String.format("Entity removed: id=%s, entity=%s", id, removed));

      } else {
        LogHelper.debug(this.getClass(),
            String.format("Attempted to remove entity, but not found: id=%s", id));
      }

    } else {
      LogHelper.error(this.getClass(), "Attempted to remove entity with null id.");
    }
  }

  private void ensureId(T entity) {
    if ((entity != null) && (entity.getId() == null)) {
      UUID id = UUID.randomUUID();

      entity.setId(id);

      LogHelper.debug(this.getClass(),
          String.format("Assigned new id to entity: id=%s, entity=%s", id, entity));
    }
  }

}
