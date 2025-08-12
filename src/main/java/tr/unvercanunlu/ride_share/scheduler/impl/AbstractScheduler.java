package tr.unvercanunlu.ride_share.scheduler.impl;

import static tr.unvercanunlu.ride_share.config.AppConfig.SCHEDULER_POLL_INTERVAL;
import static tr.unvercanunlu.ride_share.config.AppConfig.TERMINATION_TIMEOUT;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import tr.unvercanunlu.ride_share.core.log.Logger;
import tr.unvercanunlu.ride_share.dao.RideRepository;
import tr.unvercanunlu.ride_share.helper.LoggerFactory;
import tr.unvercanunlu.ride_share.scheduler.Scheduler;

public abstract class AbstractScheduler implements Scheduler {

  private static final Logger logger = LoggerFactory.getLogger(AbstractScheduler.class);

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
      logger.info("Starting Scheduler.");
      scheduler.scheduleAtFixedRate(this::job, 0, SCHEDULER_POLL_INTERVAL.toMinutes(), TimeUnit.MINUTES);
      running = true;
      logger.info("Scheduler started.");
    } else {
      logger.debug("Attempted to start Scheduler, but it is already running.");
    }
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void stop() {
    if (running) {
      logger.info("Stopping Scheduler.");
      scheduler.shutdown();

      try {
        if (!scheduler.awaitTermination(TERMINATION_TIMEOUT.toSeconds(), TimeUnit.SECONDS)) {
          logger.info("Scheduler did not terminate in time, forcing shutdownNow.");
          scheduler.shutdownNow();
        }
      } catch (InterruptedException e) {
        logger.error("Interrupted while shutting down scheduler: " + e.getMessage(), e);
        scheduler.shutdownNow();
        Thread.currentThread().interrupt();
      }

      running = false;
      logger.info("Scheduler stopped.");
    } else {
      logger.debug("Attempted to stop Scheduler, but it was not running.");
    }
  }

  protected abstract void job();

}
