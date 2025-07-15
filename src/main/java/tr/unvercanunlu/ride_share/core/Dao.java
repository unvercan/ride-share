package tr.unvercanunlu.ride_share.core;

import java.util.List;
import java.util.Optional;

public interface Dao<T, K> {

  // retrieve
  Optional<T> get(K id);

  // retrieve all
  List<T> getAll();

  // create or update
  T save(T entity);

  // delete
  void remove(K id);

}
