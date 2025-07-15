package tr.unvercanunlu.ride_share.service;

import java.math.BigDecimal;

public interface CalculationService {

  BigDecimal calculatePrice(double distance);

}
