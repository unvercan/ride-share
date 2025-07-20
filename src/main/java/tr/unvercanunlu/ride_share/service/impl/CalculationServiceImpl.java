package tr.unvercanunlu.ride_share.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import tr.unvercanunlu.ride_share.config.AppConfig;
import tr.unvercanunlu.ride_share.helper.LogHelper;
import tr.unvercanunlu.ride_share.service.CalculationService;

public class CalculationServiceImpl implements CalculationService {

  @Override
  public BigDecimal calculatePrice(double distance) {
    if (distance < 0) {
      LogHelper.error(this.getClass(),
          String.format("Invalid distance for price calculation: distance=%.2f", distance));

      throw new IllegalArgumentException("Distance cannot be negative: %.2f".formatted(distance));
    }

    try {
      BigDecimal price = BigDecimal.valueOf(
          AppConfig.BASE_FARE + (distance * AppConfig.FARE_KM_RATE)
      ).setScale(2, RoundingMode.HALF_UP);

      LogHelper.info(this.getClass(),
          String.format("Calculated price for ride: distance=%.2f, price=%s", distance, price));

      return price;

    } catch (Exception e) {
      LogHelper.error(this.getClass(),
          String.format("Error calculating price: distance=%.2f, error=%s", distance, e.getMessage()), e);

      throw e;
    }
  }

}
