package tr.unvercanunlu.ride_share.entity;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import tr.unvercanunlu.ride_share.core.entity.BaseEntity;
import tr.unvercanunlu.ride_share.status.DriverStatus;

@ToString
@Getter
@Setter
public class Driver extends BaseEntity<UUID> implements Person {

  private String name;
  private String email;
  private String phone;
  private String plate;
  private Location current;
  private DriverStatus status;

}
