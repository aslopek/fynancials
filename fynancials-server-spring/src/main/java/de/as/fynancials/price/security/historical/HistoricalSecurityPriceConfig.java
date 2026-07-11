package de.as.fynancials.price.security.historical;

import lombok.Data;

@Data
public class HistoricalSecurityPriceConfig {

  private Long id;
  private Long version;
  private Long securityId;
  private Long dataSourceId;
  private String externalSecurityId;
  private boolean active;
}
