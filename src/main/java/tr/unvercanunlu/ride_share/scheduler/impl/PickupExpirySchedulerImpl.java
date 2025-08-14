package tr.unvercanunlu.ride_share.scheduler.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import tr.unvercanunlu.ride_share.dao.DriverRepository;
import tr.unvercanunlu.ride_share.dao.RideRepository;
import tr.unvercanunlu.ride_share.entity.Ride;
import tr.unvercanunlu.ride_share.helper.TimeHelper;
import tr.unvercanunlu.ride_share.status.RideStatus;

@Slf4j
public class PickupExpirySchedulerImpl extends AbstractScheduler {

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
        log.info("Ride pickup expired. rideId={}", ride.getId());
      }

      if (expiredCount > 0) {
        log.info("Expired {} rides stuck in pickup at {}.", expiredCount, now);
      } else {
        log.debug("No rides expired in pickup phase at {}.", now);
      }

    } catch (Exception e) {
      log.error("Error occurred in PickupExpiryScheduler: {}", e.getMessage(), e);
    }
  }

}
