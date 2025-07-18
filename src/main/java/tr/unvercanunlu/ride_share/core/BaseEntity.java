package tr.unvercanunlu.ride_share.core;

import lombok.Getter;
import lombok.Setter;

public abstract class BaseEntity<K> implements Entity {

  @Setter
  @Getter
  private K id;

}
