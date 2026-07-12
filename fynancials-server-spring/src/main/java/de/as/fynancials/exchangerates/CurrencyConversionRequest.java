package de.as.fynancials.exchangerates;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CurrencyConversionRequest {

  private final BigDecimal value;
  private final LocalDate date;
}
