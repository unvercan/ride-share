package tr.unvercanunlu.ride_share.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import tr.unvercanunlu.ride_share.config.AppConfig;
import tr.unvercanunlu.ride_share.service.CalculationService;

public class CalculationServiceImpl implements CalculationService {

  @Override
  public BigDecimal calculatePrice(double distance) {
    return BigDecimal.valueOf(
        AppConfig.BASE_FARE + (distance * AppConfig.KM_RATE)
    ).setScale(2, RoundingMode.HALF_UP);
  }

}
