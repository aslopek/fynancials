package de.as.fynancials.notification.dividendannouncement;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class DividendAnnouncementUpdater {

  private final DividendAnnouncementServiceImpl dividendAnnouncementService;

  @Value("${dividend-announcement.update.enabled:false}")
  private boolean updateDividendAnnouncements;

  @Value("${dividend-announcement.remove-old.enabled:false}")
  private boolean removeOldDividendAnnouncements;

  @EventListener(ApplicationReadyEvent.class)
  void updateExchangeRates() {
    if (removeOldDividendAnnouncements) {
      dividendAnnouncementService.removeOldDividendAnnouncements();
    }
    if (updateDividendAnnouncements) {
      dividendAnnouncementService.updateDividendAnnouncements();
    }
  }
}
