package tr.unvercanunlu.ride_share.entity;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import tr.unvercanunlu.ride_share.core.BaseEntity;

@ToString
@Getter
@Setter
public class Passenger extends BaseEntity<UUID> implements Person {

  private String name;
  private String email;
  private String phone;

}
