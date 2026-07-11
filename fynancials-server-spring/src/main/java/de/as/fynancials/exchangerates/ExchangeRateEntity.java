package de.as.fynancials.exchangerates;

import de.as.fynancials.common.database.converter.BigDecimalConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Entity
@Table(name = "exchange_rate")
class ExchangeRateEntity {

  @Id
  @Column
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @CreationTimestamp
  @Column(updatable = false)
  private OffsetDateTime createdAt;

  @Column
  @UpdateTimestamp
  private OffsetDateTime updatedAt;

  @Column
  @Version
  private Long version;

  @Column(nullable = false)
  private LocalDate date;

  @Column(nullable = false)
  private String baseCurrency;

  @Column(nullable = false)
  private String targetCurrency;

  @Column(nullable = false)
  @Convert(converter = BigDecimalConverter.class)
  private BigDecimal exchangeRate;
}
