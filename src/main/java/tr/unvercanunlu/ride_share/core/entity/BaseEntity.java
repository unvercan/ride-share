package tr.unvercanunlu.ride_share.core.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
public abstract class BaseEntity<K> {

  @Setter
  @Getter
  private K id;

}
