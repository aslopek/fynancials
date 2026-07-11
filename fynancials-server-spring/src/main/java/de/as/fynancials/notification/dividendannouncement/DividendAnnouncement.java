package de.as.fynancials.notification.dividendannouncement;

import de.as.fynancials.notification.dividendannouncement.datasource.DividendAnnouncementDataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class DividendAnnouncement {

  private Long id;
  private DividendAnnouncementDataSource dataSource;
  private Long securityId;
  private boolean isNew;
  private LocalDate payDate;
  private BigDecimal amountPerShare;
  private String currency;
}
