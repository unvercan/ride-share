package tr.unvercanunlu.ride_share.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorMessage {

  public static final String DRIVER_NOT_FOUND = "Driver not found: ID=%s";
  public static final String PASSENGER_NOT_FOUND = "Passenger not found: ID=%s";
  public static final String RIDE_NOT_FOUND = "Ride not found: ID=%s";

}
