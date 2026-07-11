package de.as.fynancials.price.security.historical;

import de.as.fynancials.common.error.BadRequestException;
import de.as.fynancials.common.error.ConflictException;
import de.as.fynancials.common.error.NotFoundException;
import de.as.fynancials.exchangerates.OutdatedExchangeRateException;
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

  HistoricalSecurityPriceConfig setConfig(HistoricalSecurityPriceConfig config)
      throws BadRequestException, ConflictException;
}
