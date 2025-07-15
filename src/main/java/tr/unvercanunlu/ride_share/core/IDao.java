package tr.unvercanunlu.ride_share.core;

import java.util.Optional;

public interface IDao<T, K> {

  // retrieve
  Optional<T> get(K id);

  // create or update
  T save(T data);

  // delete
  void remove(K id);

}
