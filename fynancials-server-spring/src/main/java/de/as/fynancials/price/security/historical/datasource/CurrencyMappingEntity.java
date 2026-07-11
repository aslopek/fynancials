package de.as.fynancials.price.security.historical.datasource;

import de.as.fynancials.common.database.converter.BigDecimalConverter;
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
