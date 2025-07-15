package tr.unvercanunlu.ride_share.config;

import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tr.unvercanunlu.ride_share.status.RideStatus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AppConfig {

  public static final double BASE_FARE = 5;
  public static final double KM_RATE = 2;
  public static final int MAX_DURATION = 15;
  public static final double NEAR_DISTANCE = 15;
  public static final boolean ESTIMATION = false;

  public static final Set<RideStatus> ACTIVE_RIDE_STATUSES = Set.of(
      RideStatus.REQUESTED,
      RideStatus.ACCEPTED,
      RideStatus.STARTED
  );

}
