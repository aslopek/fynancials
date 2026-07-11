package de.as.fynancials.notification.dividendannouncement.config;

import lombok.Data;

@Data
public class DividendAnnouncementConfig {

  private long securityId;
  private Long version;
  private long dataSourceId;
  private String externalSecurityId;
  private boolean isActive;
}
