package tr.unvercanunlu.ride_share.scheduler.impl;

import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import tr.unvercanunlu.ride_share.dao.RideRepository;
import tr.unvercanunlu.ride_share.entity.Ride;
import tr.unvercanunlu.ride_share.helper.TimeHelper;
import tr.unvercanunlu.ride_share.status.RideStatus;

@Slf4j
public class RideExpirySchedulerImpl extends AbstractScheduler {

  public RideExpirySchedulerImpl(RideRepository rideRepository) {
    super(rideRepository);
  }

  @Override
  protected void job() {
    LocalDateTime now = TimeHelper.now();
    int expiredCount = 0;

    try {
      List<Ride> expiredRides = rideRepository.findAll()
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
        log.info("Ride expired. rideId={}", ride.getId());
      }

      if (expiredCount > 0) {
        log.info("Expired {} rides at {}.", expiredCount, now);
      } else {
        log.debug("No rides expired at {}.", now);
      }

    } catch (Exception e) {
      log.error("Error occurred while running RideExpiryScheduler: {}", e.getMessage(), e);
    }
  }

}
