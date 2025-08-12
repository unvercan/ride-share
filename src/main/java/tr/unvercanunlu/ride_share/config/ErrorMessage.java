package tr.unvercanunlu.ride_share.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorMessage {

  public static final String NOT_FOUND = "%s %s not found!";
  public static final String HAS_ACTIVE_RIDE = "Active ride exists for %s id=%s";
  public static final String DRIVER_UNAVAILABLE = "Driver %s unavailable!";
  public static final String DRIVER_MISSING = "Driver missing for ride %s!";
  public static final String IDENTIFIER_MISSING = "ID missing for %s!";
  public static final String RIDE_ALREADY_COMPLETED = "Ride %s already completed!";
  public static final String RIDE_ALREADY_ACCEPTED = "Ride %s already accepted by other driver!";
  public static final String RIDE_STATUS_NOT_EXPECTED = "Ride %s status not expected, expected=%s actual=%s!";

}
