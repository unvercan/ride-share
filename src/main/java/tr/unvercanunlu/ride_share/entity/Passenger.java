package tr.unvercanunlu.ride_share.entity;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import tr.unvercanunlu.ride_share.core.BaseEntity;
import tr.unvercanunlu.ride_share.dto.request.RegisterPassengerDto;

@Getter
@Setter
public class Passenger extends BaseEntity<UUID> {

  private String name;
  private String email;
  private String phone;

  public static Passenger of(RegisterPassengerDto request) {
    Passenger passenger = new Passenger();

    passenger.setName(request.name());
    passenger.setEmail(request.email());
    passenger.setPhone(request.phone());

    return passenger;
  }

}
