package tr.unvercanunlu.ride_share.status;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RideStatus {

  REQUESTED('R'),
  ACCEPTED('A'),
  APPROVED('P'),
  STARTED('S'),
  COMPLETED('C'),
  CANCELED('X');

  @Getter
  private final char code;

}
