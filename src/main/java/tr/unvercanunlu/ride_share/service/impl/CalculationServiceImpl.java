package tr.unvercanunlu.ride_share.service.impl;

import static tr.unvercanunlu.ride_share.config.AppConfig.BASE_FARE;
import static tr.unvercanunlu.ride_share.config.AppConfig.FARE_PER_KM;

import java.math.BigDecimal;
import java.math.RoundingMode;
import tr.unvercanunlu.ride_share.core.log.Logger;
import tr.unvercanunlu.ride_share.helper.LoggerFactory;
import tr.unvercanunlu.ride_share.service.CalculationService;

public class CalculationServiceImpl implements CalculationService {

  private static final Logger logger = LoggerFactory.getLogger(CalculationServiceImpl.class);

  @Override
  public BigDecimal calculatePrice(double distance) {
    if (distance < 0) {
      logger.error("Invalid distance for price calculation: distance=%.2f".formatted(distance));
      throw new IllegalArgumentException("Distance cannot be negative: %.2f".formatted(distance));
    }

    try {
      BigDecimal price = BASE_FARE.add(FARE_PER_KM.multiply(BigDecimal.valueOf(distance))).setScale(2, RoundingMode.HALF_UP);
      logger.info("Calculated price for ride: distance=%.2f, price=%s".formatted(distance, price));
      return price;

    } catch (Exception e) {
      logger.error("Error calculating price: distance=%.2f, error=%s".formatted(distance, e.getMessage()), e);
      throw e;
    }
  }

}
