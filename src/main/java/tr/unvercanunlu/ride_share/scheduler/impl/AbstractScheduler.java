package tr.unvercanunlu.ride_share.scheduler.impl;

import static tr.unvercanunlu.ride_share.config.AppConfig.SCHEDULER_POLL_INTERVAL;
import static tr.unvercanunlu.ride_share.config.AppConfig.TERMINATION_TIMEOUT;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import tr.unvercanunlu.ride_share.dao.RideRepository;
import tr.unvercanunlu.ride_share.scheduler.Scheduler;

@Slf4j
public abstract class AbstractScheduler implements Scheduler {

  protected final ScheduledExecutorService scheduler;
  protected final RideRepository rideRepository;

  protected boolean running;

  protected AbstractScheduler(RideRepository rideRepository) {
    this.rideRepository = rideRepository;
    this.scheduler = Executors.newSingleThreadScheduledExecutor();
    this.running = false;
  }

  @Override
  public void start() {
    if (!running) {
      log.info("Starting Scheduler.");
      scheduler.scheduleAtFixedRate(this::job, 0, SCHEDULER_POLL_INTERVAL.toMinutes(), TimeUnit.MINUTES);
      running = true;
      log.info("Scheduler started.");
    } else {
      log.debug("Attempted to start Scheduler, but it is already running.");
    }
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void stop() {
    if (running) {
      log.info("Stopping Scheduler.");
      scheduler.shutdown();

      try {
        if (!scheduler.awaitTermination(TERMINATION_TIMEOUT.toSeconds(), TimeUnit.SECONDS)) {
          log.info("Scheduler did not terminate in time, forcing shutdownNow.");
          scheduler.shutdownNow();
        }
      } catch (InterruptedException e) {
        log.error("Interrupted while shutting down scheduler", e);
        scheduler.shutdownNow();
        Thread.currentThread().interrupt();
      }

      running = false;
      log.info("Scheduler stopped.");
    } else {
      log.debug("Attempted to stop Scheduler, but it was not running.");
    }
  }

  protected abstract void job();

}
