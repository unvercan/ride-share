package tr.unvercanunlu.ride_share.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tr.unvercanunlu.ride_share.config.AppConfig;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogHelper {

  private static final String FORMAT = "%s: %s - %s";

  public static void info(Class<?> source, String message) {
    log(FORMAT.formatted(LogLevel.INFO.name(), source.getSimpleName(), message));
  }

  public static void error(Class<?> source, String message) {
    log(FORMAT.formatted(LogLevel.ERROR.name(), source.getSimpleName(), message));
  }

  public static void debug(Class<?> source, String message) {
    if (AppConfig.DEBUG_ENABLED) {
      log(FORMAT.formatted(LogLevel.DEBUG.name(), source.getSimpleName(), message));
    }
  }

  public static void log(LogLevel level, Class<?> source, String message) {
    log(FORMAT.formatted(level.name(), source.getSimpleName(), message));
  }

  private static void log(String statement) {
    System.out.println(statement);
  }

}
