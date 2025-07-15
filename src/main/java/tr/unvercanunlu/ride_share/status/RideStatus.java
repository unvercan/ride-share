package tr.unvercanunlu.ride_share.status;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RideStatus {

  REQUESTED('R'),
  ACCEPTED('A'),
  IN_PROGRESS('I'),
  COMPLETED('C'),
  CANCELED('X');

  private final char code;

}
