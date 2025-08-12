package tr.unvercanunlu.ride_share.core.entity;

import lombok.Getter;
import lombok.Setter;

public abstract class BaseEntity<K> {

  @Setter
  @Getter
  private K id;

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BaseEntity<?> that)) {
      return false;
    }
    if (this.id == null || that.id == null) {
      return false;
    }
    return this.id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return (id == null) ? System.identityHashCode(this) : id.hashCode();
  }

}
