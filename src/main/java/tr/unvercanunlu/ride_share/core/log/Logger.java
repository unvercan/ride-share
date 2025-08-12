package tr.unvercanunlu.ride_share.core.log;

public interface Logger {

  void log(LogLevel level, String message);

  void info(String message);

  void debug(String message);

  void error(String message);

  void error(String message, Throwable e);

}
