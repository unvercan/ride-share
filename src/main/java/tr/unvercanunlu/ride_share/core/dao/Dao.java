package tr.unvercanunlu.ride_share.core.dao;

import java.util.List;
import java.util.Optional;

public interface Dao<T, K> {

  Optional<T> findById(K id);

  List<T> findAll();

  T save(T entity);

  void deleteById(K id);

}
