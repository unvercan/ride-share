package tr.unvercanunlu.ride_share.core.log.impl;

import lombok.RequiredArgsConstructor;
import tr.unvercanunlu.ride_share.core.log.LogLevel;
import tr.unvercanunlu.ride_share.core.log.Logger;

@RequiredArgsConstructor
public abstract class BaseLogger implements Logger {

  protected final Class<?> source;

  @Override
  public void log(LogLevel level, String message) {
    switch (level) {
      case INFO -> info(message);
      case ERROR -> error(message);
      case DEBUG -> debug(message);
    }
  }

}
