package de.as.fynancials.exchangerates;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class OutdatedExchangeRateException extends RuntimeException {

  private final LocalDate date;
  private final String baseCurrency;
  private final String targetCurrency;
  private final BigDecimal exchangeRate;
  private final BigDecimal conversionResult;
}
