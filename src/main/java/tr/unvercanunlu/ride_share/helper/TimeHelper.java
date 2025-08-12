package tr.unvercanunlu.ride_share.helper;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeHelper {

  private static final Clock DEFAULT_CLOCK = Clock.systemUTC();

  private static final ThreadLocal<Clock> CLOCK = ThreadLocal.withInitial(() -> DEFAULT_CLOCK);

  public static void setClock(Clock newClock) {
    CLOCK.set(newClock);
  }

  public static void resetClock() {
    CLOCK.set(DEFAULT_CLOCK);
  }

  public static void clearClock() { // optional
    CLOCK.remove();
  }

  public static Clock clock() {
    return CLOCK.get();
  }

  public static LocalDateTime now() {
    return LocalDateTime.now(CLOCK.get());
  }

}