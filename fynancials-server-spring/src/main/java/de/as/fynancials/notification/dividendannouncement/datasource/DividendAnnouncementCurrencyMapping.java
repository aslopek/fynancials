package de.as.fynancials.notification.dividendannouncement.datasource;

import java.math.BigDecimal;
import lombok.Data;

@Data
class DividendAnnouncementCurrencyMapping {

  private String mappedCurrencyCode;
  private BigDecimal multiplier;
}
