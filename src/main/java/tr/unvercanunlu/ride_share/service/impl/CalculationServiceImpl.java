package tr.unvercanunlu.ride_share.service.impl;

import static tr.unvercanunlu.ride_share.config.AppConfig.BASE_FARE;
import static tr.unvercanunlu.ride_share.config.AppConfig.FARE_PER_KM;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.extern.slf4j.Slf4j;
import tr.unvercanunlu.ride_share.service.CalculationService;

@Slf4j
public class CalculationServiceImpl implements CalculationService {

  @Override
  public BigDecimal calculatePrice(double distance) {
    if (distance < 0) {
      log.error("Invalid distance for price calculation: distance={}", distance);
      throw new IllegalArgumentException("Distance cannot be negative: distance=%.2f".formatted(distance));
    }
    try {
      BigDecimal price = BASE_FARE.add(FARE_PER_KM.multiply(BigDecimal.valueOf(distance))).setScale(2, RoundingMode.HALF_UP);
      log.info("Calculated price for ride: distance={}, price={}", distance, price);
      return price;
    } catch (Exception ex) {
      log.error("Error calculating price: distance={}", distance, ex);
      throw ex;
    }
  }

}
