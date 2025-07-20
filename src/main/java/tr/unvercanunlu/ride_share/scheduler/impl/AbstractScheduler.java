package tr.unvercanunlu.ride_share.scheduler.impl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import tr.unvercanunlu.ride_share.config.AppConfig;
import tr.unvercanunlu.ride_share.dao.RideRepository;
import tr.unvercanunlu.ride_share.helper.LogHelper;
import tr.unvercanunlu.ride_share.scheduler.Scheduler;

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
      LogHelper.info(this.getClass(), "Starting Scheduler.");

      scheduler.scheduleAtFixedRate(this::job, 0, AppConfig.SCHEDULING_RATE_MINUTES, TimeUnit.MINUTES);

      running = true;

      LogHelper.info(this.getClass(), "Scheduler started.");

    } else {
      LogHelper.debug(this.getClass(), "Attempted to start Scheduler, but it is already running.");
    }
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void stop() {
    if (running) {
      LogHelper.info(this.getClass(), "Stopping Scheduler.");

      scheduler.shutdown();

      try {
        if (!scheduler.awaitTermination(AppConfig.TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
          LogHelper.info(this.getClass(), "Scheduler did not terminate in time, forcing shutdownNow.");

          scheduler.shutdownNow();
        }

      } catch (InterruptedException e) {
        LogHelper.error(this.getClass(), "Interrupted while shutting down scheduler: " + e.getMessage(), e);

        scheduler.shutdownNow();

        Thread.currentThread().interrupt();
      }

      running = false;

      LogHelper.info(this.getClass(), "Scheduler stopped.");

    } else {
      LogHelper.debug(this.getClass(), "Attempted to stop Scheduler, but it was not running.");
    }
  }

  protected abstract void job();

}
