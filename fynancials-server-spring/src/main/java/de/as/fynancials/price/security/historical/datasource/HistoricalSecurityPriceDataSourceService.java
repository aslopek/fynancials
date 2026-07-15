package de.as.fynancials.price.security.historical.datasource;

import de.as.fynancials.common.error.BadRequestException;
import de.as.fynancials.common.error.ConflictException;
import de.as.fynancials.common.error.NotFoundException;
import java.util.List;

public interface HistoricalSecurityPriceDataSourceService {

  HistoricalSecurityPriceDataSource createDataSource(HistoricalSecurityPriceDataSource dataSource)
      throws BadRequestException, ConflictException;

  HistoricalSecurityPriceDataSource updateDataSource(HistoricalSecurityPriceDataSource dataSource)
      throws BadRequestException, ConflictException, NotFoundException;

  void deleteDataSource(long id) throws BadRequestException, ConflictException, NotFoundException;

  List<HistoricalSecurityPriceDataSource> getDataSources();

  HistoricalSecurityPriceDataSource getDataSource(long id) throws NotFoundException;
}
