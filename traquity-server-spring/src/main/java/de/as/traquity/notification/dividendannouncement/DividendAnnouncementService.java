package de.as.traquity.notification.dividendannouncement;

import de.as.traquity.common.error.NotFoundException;
import java.util.List;

public interface DividendAnnouncementService {

  List<DividendAnnouncement> getDividendAnnouncements(Boolean isNew);

  DividendAnnouncement markAsRead(long dividendAnnouncementId) throws NotFoundException;
}
