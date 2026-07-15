package de.as.fynancials.notification.dividendannouncement.config;

import de.as.fynancials.common.error.BadRequestException;
import de.as.fynancials.common.error.ConflictException;
import de.as.fynancials.common.error.NotFoundException;
import java.util.List;

public interface DividendAnnouncementConfigService {

  List<DividendAnnouncementConfig> getDividendAnnouncementConfigs();

  DividendAnnouncementConfig getDividendAnnouncementConfig(long securityId) throws NotFoundException;

  DividendAnnouncementConfig createDividendAnnouncementConfig(DividendAnnouncementConfig dividendAnnouncementConfig)
      throws BadRequestException, ConflictException;

  DividendAnnouncementConfig updateDividendAnnouncementConfig(DividendAnnouncementConfig dividendAnnouncementConfig)
      throws BadRequestException, ConflictException, NotFoundException;

  void deleteDividendAnnouncementConfig(long securityId) throws NotFoundException;
}
