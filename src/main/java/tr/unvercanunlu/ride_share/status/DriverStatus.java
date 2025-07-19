package tr.unvercanunlu.ride_share.status;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
public enum DriverStatus {

  AVAILABLE('A'),
  BUSY('B'),
  OFFLINE('O');

  @Getter
  private final char code;

}
