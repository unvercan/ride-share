package tr.unvercanunlu.ride_share.config;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tr.unvercanunlu.ride_share.status.RideStatus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AppConfig {

  // fare
  public static final BigDecimal BASE_FARE = new BigDecimal("5.00");
  public static final BigDecimal FARE_PER_KM = new BigDecimal("2.00");

  // duration
  public static final Duration MAX_WAIT = Duration.ofMinutes(15);
  public static final Duration TERMINATION_TIMEOUT = Duration.ofSeconds(10);
  public static final Duration SCHEDULER_POLL_INTERVAL = Duration.ofMinutes(1);

  // log
  public static final boolean DEBUG_ENABLED = false;
  public static final String LOG_FORMAT = "%s - %s: %s - %s";

  public static final double MAX_PICKUP_RADIUS_KM = 15;

  public static final boolean ESTIMATION_ENABLED = true;

  public static final Set<RideStatus> ACTIVE_RIDE_STATES = Set.of(
      RideStatus.REQUESTED,
      RideStatus.ACCEPTED,
      RideStatus.APPROVED,
      RideStatus.STARTED
  );

}
