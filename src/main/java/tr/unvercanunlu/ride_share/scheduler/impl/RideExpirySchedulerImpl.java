package tr.unvercanunlu.ride_share.scheduler.impl;

import java.time.LocalDateTime;
import java.util.List;
import tr.unvercanunlu.ride_share.dao.RideRepository;
import tr.unvercanunlu.ride_share.entity.Ride;
import tr.unvercanunlu.ride_share.helper.LogHelper;
import tr.unvercanunlu.ride_share.status.RideStatus;

public class RideExpirySchedulerImpl extends AbstractScheduler {

  public RideExpirySchedulerImpl(RideRepository rideRepository) {
    super(rideRepository);
  }

  @Override
  protected void job() {
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
          String.format("Error occurred while running RideExpiryScheduler: %s", e.getMessage()), e);
    }
  }

}
