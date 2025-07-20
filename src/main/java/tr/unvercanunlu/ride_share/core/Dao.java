package tr.unvercanunlu.ride_share.core;

import java.util.List;
import java.util.Optional;

public interface Dao<T, K> {

  Optional<T> get(K id);

  List<T> getAll();

  T save(T entity);

  void remove(K id);

}
