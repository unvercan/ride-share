package tr.unvercanunlu.ride_share.scheduler;

public interface Scheduler {

  void start();

  boolean isRunning();

  void stop();

}
