package tr.unvercanunlu.ride_share.scheduler.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import tr.unvercanunlu.ride_share.dao.DriverRepository;
import tr.unvercanunlu.ride_share.dao.RideRepository;
import tr.unvercanunlu.ride_share.entity.Ride;
import tr.unvercanunlu.ride_share.helper.LogHelper;
import tr.unvercanunlu.ride_share.status.RideStatus;

public class PickupExpirySchedulerImpl extends AbstractScheduler {

  private final DriverRepository driverRepository;

  public PickupExpirySchedulerImpl(RideRepository rideRepository, DriverRepository driverRepository) {
    super(rideRepository);

    this.driverRepository = driverRepository;
  }

  @Override
  protected void job() {
    LocalDateTime now = LocalDateTime.now();

    int expiredCount = 0;

    try {
      List<Ride> ridesToExpire = rideRepository.getAll()
          .stream()
          .filter(ride -> ride != null && ride.getStatus() == RideStatus.APPROVED)
          .filter(ride -> ride.getPickupEndAt() != null && ride.getPickupEndAt().isBefore(now))
          .toList();

      for (Ride ride : ridesToExpire) {
        UUID previousDriverId = ride.getDriverId();

        ride.setExpiredAt(now);
        ride.setStatus(RideStatus.EXPIRED);

        rideRepository.save(ride);

        Optional.ofNullable(previousDriverId)
            .ifPresent(driverRepository::updateAsAvailable);

        expiredCount++;

        LogHelper.info(this.getClass(),
            String.format("Ride pickup expired. rideId=%s", ride.getId()));
      }

      if (expiredCount > 0) {
        LogHelper.info(this.getClass(),
            String.format("Expired %d rides stuck in pickup at %s.", expiredCount, now));
      } else {
        LogHelper.debug(this.getClass(),
            String.format("No rides expired in pickup phase at %s.", now));
      }

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Error occurred in PickupExpiryScheduler: %s", e.getMessage()), e);
    }
  }

}
