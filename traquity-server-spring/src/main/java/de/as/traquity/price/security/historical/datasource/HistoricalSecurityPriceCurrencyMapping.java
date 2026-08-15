package de.as.traquity.price.security.historical.datasource;

import java.math.BigDecimal;
import lombok.Data;

@Data
class HistoricalSecurityPriceCurrencyMapping {

  private String mappedCurrencyCode;
  private BigDecimal multiplier;
}
