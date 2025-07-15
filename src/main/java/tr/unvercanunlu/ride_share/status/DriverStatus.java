package tr.unvercanunlu.ride_share.status;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DriverStatus {

  AVAILABLE('A'),
  BUSY('B'),
  OFFLINE('O');

  private final char code;

}
