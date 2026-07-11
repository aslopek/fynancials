package de.as.fynancials.notification.dividendannouncement.datasource;

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
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Entity
@Table(name = "DIVIDEND_ANNOUNCEMENT_DATA_SOURCE")
class DividendAnnouncementDataSourceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @Lob
  @Column(name = "URL_PATTERN", nullable = false)
  private String urlPattern;

  @Lob
  @Column(name = "JSON_PATH_DATE", nullable = false)
  private String jsonPathDate;

  @Column(name = "DATE_FORMAT")
  private String dateFormat;

  @Lob
  @Column(name = "JSON_PATH_VALUE", nullable = false)
  private String jsonPathValue;

  @Lob
  @Column(name = "JSON_PATH_CURRENCY")
  private String jsonPathCurrency;

  @Lob
  @Column(name = "REGEX_CURRENCY")
  private String regexCurrency;

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

  @ElementCollection(fetch = EAGER)
  @CollectionTable(
      name = "DIVIDEND_ANNOUNCEMENT_DATA_SOURCE_REQUEST_HEADER",
      joinColumns = @JoinColumn(name = "DATA_SOURCE_ID")
  )
  @MapKeyColumn(name = "HEADER_NAME")
  @Column(name = "HEADER_VALUE", nullable = false, columnDefinition = "CLOB")
  private Map<String, String> requestHeaders = new HashMap<>();

  @ElementCollection(fetch = EAGER)
  @CollectionTable(
      name = "DIVIDEND_ANNOUNCEMENT_DATA_SOURCE_CURRENCY_MAPPING",
      joinColumns = @JoinColumn(name = "DATA_SOURCE_ID")
  )
  @MapKeyColumn(name = "CURRENCY_KEY")
  private Map<String, CurrencyMappingEntity> currencyMappings = new HashMap<>();
}
