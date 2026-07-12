package de.as.fynancials.exchangerates;

import de.as.fynancials.common.error.NotFoundException;
import java.math.BigDecimal;
import java.util.List;

public interface ExchangeRateService {

  /**
   * Converts every item from {@code baseCurrency} to {@code targetCurrency} using its own date, fetching the
   * applicable exchange rates only once for the whole list instead of once per item - even a single-item list is
   * cheap, so this is the only conversion entry point; there is deliberately no single-value overload, to avoid
   * reintroducing an accidental per-item DB round trip in a loop. The result has the same size and ordering as
   * {@code items}.
   *
   * @throws NotFoundException             when no conversion rate between the two currencies can be found for one of the items
   * @throws OutdatedExchangeRateException when a conversion rate could be found for one of the items, but its value is outdated
   */
  List<BigDecimal> convert(List<CurrencyConversionRequest> items, String baseCurrency, String targetCurrency)
      throws NotFoundException, OutdatedExchangeRateException;
}
