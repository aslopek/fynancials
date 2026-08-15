package de.as.traquity.notification.dividendannouncement.datasource;

import de.as.traquity.common.database.converter.BigDecimalConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Embeddable
class CurrencyMappingEntity {

  @Column(name = "MAPPED_CURRENCY_CODE", nullable = false, length = 3)
  private String mappedCurrencyCode;

  @Column(name = "MULTIPLIER")
  @Convert(converter = BigDecimalConverter.class)
  private BigDecimal multiplier;
}
