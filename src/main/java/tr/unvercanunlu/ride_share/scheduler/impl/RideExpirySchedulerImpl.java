package tr.unvercanunlu.ride_share.scheduler.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import tr.unvercanunlu.ride_share.config.AppConfig;
import tr.unvercanunlu.ride_share.dao.RideRepository;
import tr.unvercanunlu.ride_share.entity.Ride;
import tr.unvercanunlu.ride_share.helper.LogHelper;
import tr.unvercanunlu.ride_share.scheduler.Scheduler;
import tr.unvercanunlu.ride_share.status.RideStatus;

public class RideExpirySchedulerImpl implements Scheduler {

  private final ScheduledExecutorService scheduler;
  private final RideRepository rideRepository;
  private boolean running;

  public RideExpirySchedulerImpl(RideRepository rideRepository) {
    this.rideRepository = rideRepository;

    this.scheduler = Executors.newSingleThreadScheduledExecutor();
    this.running = false;
  }

  @Override
  public void start() {
    if (!running) {
      LogHelper.info(this.getClass(), "Starting RideExpiryScheduler.");

      scheduler.scheduleAtFixedRate(this::updateExpiredRides, 0, AppConfig.SCHEDULING_RATE_MINUTES, TimeUnit.MINUTES);

      running = true;

      LogHelper.info(this.getClass(), "RideExpiryScheduler started.");

    } else {
      LogHelper.debug(this.getClass(), "Attempted to start RideExpiryScheduler, but it is already running.");
    }
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void stop() {
    if (running) {
      LogHelper.info(this.getClass(), "Stopping RideExpiryScheduler.");

      scheduler.shutdown();

      try {
        if (!scheduler.awaitTermination(AppConfig.TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
          LogHelper.info(this.getClass(), "Scheduler did not terminate in time, forcing shutdownNow.");

          scheduler.shutdownNow();
        }

      } catch (InterruptedException ex) {
        LogHelper.error(this.getClass(), "Interrupted while shutting down scheduler: " + ex.getMessage());

        scheduler.shutdownNow();

        Thread.currentThread().interrupt();
      }

      running = false;

      LogHelper.info(this.getClass(), "RideExpiryScheduler stopped.");

    } else {
      LogHelper.debug(this.getClass(), "Attempted to stop RideExpiryScheduler, but it was not running.");
    }
  }

  private void updateExpiredRides() {
    LocalDateTime now = LocalDateTime.now();

    int expiredCount = 0;

    try {
      List<Ride> expiredRides = rideRepository.getAll()
          .stream()
          .filter(ride -> ride != null && ride.getStatus() != null && ride.getRequestedAt() != null)
          .filter(ride -> ride.getStatus().equals(RideStatus.REQUESTED))
          .filter(ride -> ride.getRequestEndAt().isBefore(now))
          .toList();

      for (Ride ride : expiredRides) {
        ride.setExpiredAt(now);
        ride.setStatus(RideStatus.EXPIRED);
        rideRepository.save(ride);
        expiredCount++;

        LogHelper.info(this.getClass(),
            String.format("Ride expired. rideId=%s", ride.getId()));
      }

      if (expiredCount > 0) {
        LogHelper.info(this.getClass(),
            String.format("Expired %d rides at %s.", expiredCount, now));
      } else {
        LogHelper.debug(this.getClass(),
            String.format("No rides expired at %s.", now));
      }

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Error occurred while running RideExpiryScheduler: %s", e.getMessage()));
    }
  }

}
