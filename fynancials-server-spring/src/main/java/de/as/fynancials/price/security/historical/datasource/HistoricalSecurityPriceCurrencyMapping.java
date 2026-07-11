package de.as.fynancials.price.security.historical.datasource;

import java.math.BigDecimal;
import lombok.Data;

@Data
class HistoricalSecurityPriceCurrencyMapping {

  private String mappedCurrencyCode;
  private BigDecimal multiplier;
}
