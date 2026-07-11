package de.as.fynancials.price.security.historical.datasource;

import de.as.fynancials.common.monetary.CurrencyMapping;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Data;

@Data
public class HistoricalSecurityPriceDataSource implements CurrencyMapping {

  private Long id;
  private String name;
  private String jsonPathDate;
  private String dateFormat;
  private String jsonPathValue;
  private String jsonPathCurrency;
  private String regexCurrency;
  private Integer regexCurrencyGroup;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
  private Long version;
  private Map<Integer, String> urlPatterns = new HashMap<>();
  private Map<String, String> requestHeaders = new HashMap<>();
  private Map<String, HistoricalSecurityPriceCurrencyMapping> currencyMappings = new HashMap<>();
  private Set<HistoricalSecurityPriceMarketCloseTime> marketCloseTimes = new HashSet<>();

  @Override
  public String getMappedCurrencyCode(String currencyKey) {
    HistoricalSecurityPriceCurrencyMapping mapping = getMapping(currencyKey);
    if (mapping == null) {
      return null;
    } else {
      return mapping.getMappedCurrencyCode();
    }
  }

  @Override
  public BigDecimal getMultiplier(String currencyKey) {
    HistoricalSecurityPriceCurrencyMapping mapping = getMapping(currencyKey);
    if (mapping == null) {
      return null;
    } else {
      return mapping.getMultiplier();
    }
  }

  private HistoricalSecurityPriceCurrencyMapping getMapping(String currencyKey) {
    return currencyMappings.get(currencyKey);
  }
}
