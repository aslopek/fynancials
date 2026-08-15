package de.as.traquity.notification.dividendannouncement.datasource;

import java.math.BigDecimal;
import lombok.Data;

@Data
class DividendAnnouncementCurrencyMapping {

  private String mappedCurrencyCode;
  private BigDecimal multiplier;
}
