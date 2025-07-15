package tr.unvercanunlu.ride_share.core;

import lombok.Getter;
import lombok.Setter;

public abstract class BaseEntity<K> {

  @Setter
  @Getter
  private K id;

}
