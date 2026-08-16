package de.as.traquity.price.security.historical;

import de.as.traquity.common.error.BadRequestException;
import de.as.traquity.common.error.ConflictException;
import de.as.traquity.common.error.NotFoundException;
import de.as.traquity.exchangerates.OutdatedExchangeRateException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface HistoricalSecurityPriceService {

  List<HistoricalSecurityPrice> getPrices(long securityId, LocalDate startDate)
      throws BadRequestException, NotFoundException;

  List<HistoricalSecurityPrice> getPrices(long securityId, LocalDate startDate, String currency)
      throws BadRequestException, NotFoundException, OutdatedExchangeRateException;

  HistoricalSecurityPrice getLatestPrice(long securityId, String currency) throws NotFoundException;

  void splitAdjustment(Long securityId, BigDecimal multiplier, LocalDate exDate);

  HistoricalSecurityPriceConfig getConfig(long securityId) throws NotFoundException;

  HistoricalSecurityPriceConfig createConfig(HistoricalSecurityPriceConfig config)
      throws BadRequestException, ConflictException, NotFoundException;

  HistoricalSecurityPriceConfig updateConfig(HistoricalSecurityPriceConfig config)
      throws BadRequestException, ConflictException, NotFoundException;
}
