package de.as.fynancials.price.security.historical.datasource;

import static jakarta.persistence.FetchType.EAGER;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "HISTORICAL_SECURITY_PRICE_DATA_SOURCE")
class HistoricalSecurityPriceDataSourceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @Lob
  @Column(name = "JSON_PATH_DATE", nullable = false)
  private String jsonPathDate;

  @Column(name = "DATE_FORMAT")
  private String dateFormat;

  @Lob
  @Column(name = "JSON_PATH_VALUE", nullable = false)
  private String jsonPathValue;

  @Lob
  @Column(name = "JSON_PATH_CURRENCY", nullable = false)
  private String jsonPathCurrency;

  @Lob
  @Column(name = "REGEX_CURRENCY")
  private String regexCurrency;

  @Lob
  @Column(name = "REGEX_CURRENCY_GROUP")
  private Integer regexCurrencyGroup;

  @CreationTimestamp
  @Column(name = "CREATED_AT", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "UPDATED_AT", nullable = false)
  private OffsetDateTime updatedAt;

  @Version
  private Long version;

  /**
   * Currency mappings: Key is 'timespan in days', value is URL pattern.
   */
  @ElementCollection(fetch = EAGER)
  @CollectionTable(
      name = "HISTORICAL_SECURITY_PRICE_DATA_SOURCE_URL",
      joinColumns = @JoinColumn(name = "DATA_SOURCE_ID")
  )
  @MapKeyColumn(name = "TIMESPAN_IN_DAYS")
  @Column(name = "URL_PATTERN", nullable = false, columnDefinition = "CLOB")
  private Map<Integer, String> urlPatterns = new HashMap<>();

  /**
   * HTTP headers: Key is header name, value is header value.
   */
  @ElementCollection(fetch = EAGER)
  @CollectionTable(
      name = "HISTORICAL_SECURITY_PRICE_DATA_SOURCE_REQUEST_HEADER",
      joinColumns = @JoinColumn(name = "DATA_SOURCE_ID")
  )
  @MapKeyColumn(name = "HEADER_NAME")
  @Column(name = "HEADER_VALUE", nullable = false, columnDefinition = "CLOB")
  private Map<String, String> requestHeaders = new HashMap<>();

  /**
   * Currency mappings: Key is the currency identifier as returned by the API, vale is the mapped currency and optionally a multiplier for conversion.
   */
  @ElementCollection(fetch = EAGER)
  @CollectionTable(
      name = "HISTORICAL_SECURITY_PRICE_DATA_SOURCE_CURRENCY_MAPPING",
      joinColumns = @JoinColumn(name = "DATA_SOURCE_ID")
  )
  @MapKeyColumn(name = "CURRENCY_KEY")
  private Map<String, CurrencyMappingEntity> currencyMappings = new HashMap<>();

  @ElementCollection(fetch = EAGER)
  @CollectionTable(
      name = "HISTORICAL_SECURITY_PRICE_MARKET_CLOSE_TIME",
      joinColumns = @JoinColumn(name = "DATA_SOURCE_ID"),
      uniqueConstraints = @UniqueConstraint(
          name = "UK_HSP_MARKET_CLOSE_TIME",
          columnNames = {"DATA_SOURCE_ID", "TIME_ZONE"}
      )
  )
  private Set<HistoricalSecurityPriceMarketCloseTimeEntity> marketCloseTimes = new HashSet<>();
}
