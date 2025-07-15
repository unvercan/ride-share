package tr.unvercanunlu.ride_share.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AppConfig {

  public static final double BASE_FARE = 5;
  public static final double PER_KM_RATE = 2;
  public static final int MAX_DURATION = 15;
  public static final double NEAR_DISTANCE = 15;
  public static final boolean ESTIMATION = false;

}
