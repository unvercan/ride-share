package tr.unvercanunlu.ride_share.scheduler.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import tr.unvercanunlu.ride_share.core.log.Logger;
import tr.unvercanunlu.ride_share.dao.DriverRepository;
import tr.unvercanunlu.ride_share.dao.RideRepository;
import tr.unvercanunlu.ride_share.entity.Ride;
import tr.unvercanunlu.ride_share.helper.LoggerFactory;
import tr.unvercanunlu.ride_share.helper.TimeHelper;
import tr.unvercanunlu.ride_share.status.RideStatus;

public class PickupExpirySchedulerImpl extends AbstractScheduler {

  private static final Logger logger = LoggerFactory.getLogger(PickupExpirySchedulerImpl.class);

  private final DriverRepository driverRepository;

  public PickupExpirySchedulerImpl(RideRepository rideRepository, DriverRepository driverRepository) {
    super(rideRepository);
    this.driverRepository = driverRepository;
  }

  @Override
  protected void job() {
    LocalDateTime now = TimeHelper.now();
    int expiredCount = 0;

    try {
      List<Ride> ridesToExpire = rideRepository.findAll()
          .stream()
          .filter(ride -> ride != null && ride.getStatus() == RideStatus.APPROVED)
          .filter(ride -> ride.getPickupEndAt() != null && ride.getPickupEndAt().isBefore(now))
          .toList();

      for (Ride ride : ridesToExpire) {
        UUID previousDriverId = ride.getDriverId();
        ride.setExpiredAt(now);
        ride.setStatus(RideStatus.EXPIRED);
        rideRepository.save(ride);
        Optional.ofNullable(previousDriverId).ifPresent(driverRepository::setAvailable);
        expiredCount++;
        logger.info("Ride pickup expired. rideId=%s".formatted(ride.getId()));
      }

      if (expiredCount > 0) {
        logger.info("Expired %d rides stuck in pickup at %s.".formatted(expiredCount, now));
      } else {
        logger.debug("No rides expired in pickup phase at %s.".formatted(now));
      }

    } catch (Exception e) {
      logger.error("Error occurred in PickupExpiryScheduler: %s".formatted(e.getMessage()), e);
    }
  }

}
