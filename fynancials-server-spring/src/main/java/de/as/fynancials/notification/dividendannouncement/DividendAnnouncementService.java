package de.as.fynancials.notification.dividendannouncement;

import de.as.fynancials.common.error.NotFoundException;
import java.util.List;

public interface DividendAnnouncementService {

  List<DividendAnnouncement> getDividendAnnouncements(Boolean isNew);

  void markAsRead(long dividendAnnouncementId) throws NotFoundException;
}
