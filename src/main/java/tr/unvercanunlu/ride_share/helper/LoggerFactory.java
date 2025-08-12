package tr.unvercanunlu.ride_share.helper;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tr.unvercanunlu.ride_share.core.log.Logger;
import tr.unvercanunlu.ride_share.core.log.impl.ConsoleLogger;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoggerFactory {

  private static final ConcurrentMap<Class<?>, Logger> loggers = new ConcurrentHashMap<>();

  public static Logger getLogger(Class<?> source) {
    loggers.putIfAbsent(source, new ConsoleLogger(source));
    return loggers.get(source);
  }

}
