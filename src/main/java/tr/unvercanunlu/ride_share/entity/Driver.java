package tr.unvercanunlu.ride_share.entity;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import tr.unvercanunlu.ride_share.core.BaseEntity;
import tr.unvercanunlu.ride_share.dto.request.RegisterDriverDto;
import tr.unvercanunlu.ride_share.status.DriverStatus;

@Getter
@Setter
public class Driver extends BaseEntity<UUID> {

  private String name;
  private String email;
  private String phone;
  private String plate;
  private Location current;
  private DriverStatus status;

  public static Driver of(RegisterDriverDto request) {
    Driver driver = new Driver();

    driver.setName(request.name());
    driver.setEmail(request.email());
    driver.setPhone(request.phone());
    driver.setPlate(request.plate());
    driver.setStatus(DriverStatus.OFFLINE);

    return driver;
  }

}
