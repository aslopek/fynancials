package de.as.fynancials.exchangerates;

import de.as.fynancials.common.error.NotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface ExchangeRateService {

  /**
   * @throws NotFoundException             when no conversion rate between the two currencies can be found
   * @throws OutdatedExchangeRateException when a conversion rate between currencies could be found, but its value is
   *                                       outdated
   */
  BigDecimal convert(BigDecimal value, String baseCurrency, String targetCurrency, LocalDate date)
      throws NotFoundException, OutdatedExchangeRateException;
}
