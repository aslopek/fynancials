package de.as.fynancials.notification.dividendannouncement.datasource;

import de.as.fynancials.common.monetary.CurrencyMapping;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class DividendAnnouncementDataSource implements CurrencyMapping {

  private Long id;
  private String name;
  private String urlPattern;
  private String jsonPathDate;
  private String dateFormat;
  private String jsonPathValue;
  private String jsonPathCurrency;
  private String regexCurrency;
  private Integer regexCurrencyGroup;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
  private Long version;
  private Map<String, String> requestHeaders = new HashMap<>();
  private Map<String, DividendAnnouncementCurrencyMapping> currencyMappings = new HashMap<>();

  @Override
  public String getMappedCurrencyCode(String currencyKey) {
    DividendAnnouncementCurrencyMapping mapping = getMapping(currencyKey);
    if (mapping == null) {
      return null;
    } else {
      return mapping.getMappedCurrencyCode();
    }
  }

  @Override
  public BigDecimal getMultiplier(String currencyKey) {
    DividendAnnouncementCurrencyMapping mapping = getMapping(currencyKey);
    if (mapping == null) {
      return null;
    } else {
      return mapping.getMultiplier();
    }
  }

  private DividendAnnouncementCurrencyMapping getMapping(String currencyKey) {
    return currencyMappings.get(currencyKey);
  }
}
