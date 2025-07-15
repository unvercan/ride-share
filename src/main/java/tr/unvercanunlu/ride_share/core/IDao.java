package tr.unvercanunlu.ride_share.core;

public interface IDao<T, K> {

  // retrieve
  T get(K id);

  // create or update
  T save(T data);

  // delete
  void remove(K id);

}
