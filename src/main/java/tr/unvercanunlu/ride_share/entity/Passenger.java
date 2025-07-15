package tr.unvercanunlu.ride_share.entity;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import tr.unvercanunlu.ride_share.core.BaseEntity;

@Getter
@Setter
public class Passenger extends BaseEntity<UUID> {

  private String name;
  private String email;
  private String phone;

}
