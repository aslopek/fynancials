package de.as.fynancials.price.security.historical.datasource;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
class HistoricalSecurityPriceMarketCloseTimeEntity {

  @Column(name = "TIME", nullable = false, length = 8)
  private String time;

  @Column(name = "TIME_ZONE", nullable = false)
  private String timeZone;
}
