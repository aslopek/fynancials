package de.as.fynancials.price.security.historical;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class HistoricalSecurityPrice {

  private Long securityId;
  private BigDecimal price;
  private String currency;
  private LocalDate date;
}
