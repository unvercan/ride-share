package tr.unvercanunlu.ride_share.core.log.impl;

import static tr.unvercanunlu.ride_share.config.AppConfig.DEBUG_ENABLED;
import static tr.unvercanunlu.ride_share.config.AppConfig.LOG_FORMAT;

import tr.unvercanunlu.ride_share.core.log.LogLevel;
import tr.unvercanunlu.ride_share.helper.TimeHelper;

public class ConsoleLogger extends BaseLogger {

  public ConsoleLogger(Class<?> source) {
    super(source);
  }

  @Override
  public void info(String message) {
    log(LOG_FORMAT.formatted(TimeHelper.now(), LogLevel.INFO.name(), source.getSimpleName(), message));
  }

  @Override
  public void debug(String message) {
    if (DEBUG_ENABLED) {
      log(LOG_FORMAT.formatted(TimeHelper.now(), LogLevel.DEBUG.name(), source.getSimpleName(), message));
    }
  }

  @Override
  public void error(String message) {
    logError(LOG_FORMAT.formatted(TimeHelper.now(), LogLevel.ERROR.name(), source.getSimpleName(), message));
  }

  @Override
  public void error(String message, Throwable e) {
    logError(LOG_FORMAT.formatted(TimeHelper.now(), LogLevel.ERROR.name(), source.getSimpleName(), message));

    if (e != null) {
      e.printStackTrace();
    }
  }

  private static void log(String statement) {
    System.out.println(statement);
  }

  private static void logError(String statement) {
    System.err.println(statement);
  }

}
