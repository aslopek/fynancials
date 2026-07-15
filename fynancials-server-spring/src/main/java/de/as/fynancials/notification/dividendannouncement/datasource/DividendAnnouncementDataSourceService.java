package de.as.fynancials.notification.dividendannouncement.datasource;

import de.as.fynancials.common.error.BadRequestException;
import de.as.fynancials.common.error.ConflictException;
import de.as.fynancials.common.error.NotFoundException;
import java.util.List;

public interface DividendAnnouncementDataSourceService {

  DividendAnnouncementDataSource createDataSource(DividendAnnouncementDataSource dataSource)
      throws BadRequestException, ConflictException;

  DividendAnnouncementDataSource updateDataSource(DividendAnnouncementDataSource dataSource)
      throws BadRequestException, ConflictException, NotFoundException;

  void deleteDataSource(long id) throws BadRequestException, ConflictException, NotFoundException;

  boolean dataSourceExists(long id);

  List<DividendAnnouncementDataSource> getDataSources();

  DividendAnnouncementDataSource getDataSource(long id) throws NotFoundException;
}
