package tr.unvercanunlu.ride_share.core;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class BaseDao<T extends BaseEntity<UUID>> implements IDao<T, UUID> {

  protected final ConcurrentMap<UUID, T> entities = new ConcurrentHashMap<>();

  @Override
  public T get(UUID id) {
    return entities.get(id);
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
