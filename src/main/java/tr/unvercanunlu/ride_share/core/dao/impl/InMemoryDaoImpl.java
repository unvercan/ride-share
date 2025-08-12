package tr.unvercanunlu.ride_share.core.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import tr.unvercanunlu.ride_share.core.dao.Dao;
import tr.unvercanunlu.ride_share.core.entity.BaseEntity;
import tr.unvercanunlu.ride_share.core.log.Logger;
import tr.unvercanunlu.ride_share.helper.LoggerFactory;

public abstract class InMemoryDaoImpl<T extends BaseEntity<UUID>> implements Dao<T, UUID> {

  private static final Logger logger = LoggerFactory.getLogger(InMemoryDaoImpl.class);

  protected final ConcurrentMap<UUID, T> entities = new ConcurrentHashMap<>();

  @Override
  public Optional<T> findById(UUID id) {
    Optional<T> result = Optional.ofNullable(id).map(entities::get);
    if (result.isPresent()) {
      logger.info(String.format("Entity found: id=%s, entity=%s", id, result.get()));
    } else {
      logger.debug(String.format("Entity not found: id=%s", id));
    }
    return result;
  }

  @Override
  public List<T> findAll() {
    logger.debug(String.format("Retrieving all entities. Count=%d", entities.size()));
    return new ArrayList<>(entities.values());
  }

  @Override
  public T save(T entity) {
    if (entity == null) {
      logger.error("Attempted to save null entity.");
      return null;
    }
    ensureId(entity);
    entities.put(entity.getId(), entity);
    logger.info(String.format("Entity saved: id=%s, entity=%s", entity.getId(), entity));
    return entity;
  }

  @Override
  public void deleteById(UUID id) {
    if (id != null) {
      T removed = entities.remove(id);
      if (removed != null) {
        logger.info(String.format("Entity removed: id=%s, entity=%s", id, removed));
      } else {
        logger.debug(String.format("Attempted to remove entity, but not found: id=%s", id));
      }
    } else {
      logger.error("Attempted to remove entity with null id.");
    }
  }

  private void ensureId(T entity) {
    if ((entity != null) && (entity.getId() == null)) {
      UUID id = UUID.randomUUID();
      entity.setId(id);
      logger.debug(String.format("Assigned new id to entity: id=%s, entity=%s", id, entity));
    }
  }

}
