package tr.unvercanunlu.ride_share.core.dao.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import tr.unvercanunlu.ride_share.core.dao.Dao;
import tr.unvercanunlu.ride_share.core.entity.BaseEntity;
import tr.unvercanunlu.ride_share.util.ValidationUtil;

@Slf4j
public abstract class InMemoryDaoImpl<T extends BaseEntity<UUID>> implements Dao<T, UUID> {

  protected final ConcurrentMap<UUID, T> entities = new ConcurrentHashMap<>();

  @Override
  public Optional<T> findById(UUID id) {
    ValidationUtil.checkIdNotNull(id);
    Optional<T> result = Optional.ofNullable(entities.get(id));
    if (result.isPresent()) {
      log.info("Entity found: id={}", id);
    } else {
      log.debug("Entity not found: id={}", id);
    }
    return result;
  }

  @Override
  public List<T> findAll() {
    log.debug("Retrieving all entities. Count={}", entities.size());
    return List.copyOf(entities.values());
  }

  @Override
  public T save(T entity) {
    if (entity == null) {
      throw new IllegalArgumentException("Entity missing!");
    }
    ensureId(entity);
    entities.put(entity.getId(), entity);
    log.info("Entity saved: id={}", entity.getId());
    return entity;
  }

  @Override
  public boolean deleteById(UUID id) {
    ValidationUtil.checkIdNotNull(id);
    T removed = entities.remove(id);
    if (removed != null) {
      log.info("Entity removed: id={}", id);
    } else {
      log.debug("Attempted to remove entity, but not found: id={}", id);
    }
    return (removed != null);
  }

  private void ensureId(T entity) {
    if (entity.getId() != null) {
      return;
    }
    UUID id = UUID.randomUUID();
    entity.setId(id);
    log.debug("Assigned new id to entity: id={}", id);
  }

}
