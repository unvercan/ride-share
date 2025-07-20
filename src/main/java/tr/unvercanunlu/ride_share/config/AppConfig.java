package tr.unvercanunlu.ride_share.config;

import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tr.unvercanunlu.ride_share.status.RideStatus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AppConfig {

  public static final double BASE_FARE = 5;
  public static final double FARE_KM_RATE = 2;
  public static final int MAX_DURATION_MINUTES = 15;
  public static final double NEAR_DISTANCE_KM = 15;
  public static final boolean ESTIMATION = true;
  public static final int TERMINATION_TIMEOUT_SECONDS = 10;
  public static final int SCHEDULING_RATE_MINUTES = 1;

  public static final Set<RideStatus> ACTIVE_RIDE_STATUSES = Set.of(
      RideStatus.REQUESTED,
      RideStatus.ACCEPTED,
      RideStatus.APPROVED,
      RideStatus.STARTED
  );

  public static final boolean DEBUG_ENABLED = false;
  public static final String LOG_FORMAT = "%s - %s: %s - %s";

}
