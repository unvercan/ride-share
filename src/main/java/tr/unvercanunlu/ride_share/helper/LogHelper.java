package tr.unvercanunlu.ride_share.helper;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tr.unvercanunlu.ride_share.config.AppConfig;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogHelper {

  public static void info(Class<?> source, String message) {
    log(AppConfig.LOG_FORMAT.formatted(LocalDateTime.now(), LogLevel.INFO.name(), source.getSimpleName(), message));
  }

  public static void error(Class<?> source, String message) {
    logError(AppConfig.LOG_FORMAT.formatted(LocalDateTime.now(), LogLevel.ERROR.name(), source.getSimpleName(), message));
  }

  public static void error(Class<?> source, String message, Throwable e) {
    logError(AppConfig.LOG_FORMAT.formatted(LocalDateTime.now(), LogLevel.ERROR.name(), source.getSimpleName(), message));

    if (e != null) {
      e.printStackTrace();
    }
  }

  public static void debug(Class<?> source, String message) {
    if (AppConfig.DEBUG_ENABLED) {
      log(AppConfig.LOG_FORMAT.formatted(LocalDateTime.now(), LogLevel.DEBUG.name(), source.getSimpleName(), message));
    }
  }

  private static void log(String statement) {
    System.out.println(statement);
  }

  private static void logError(String statement) {
    System.err.println(statement);
  }

}
