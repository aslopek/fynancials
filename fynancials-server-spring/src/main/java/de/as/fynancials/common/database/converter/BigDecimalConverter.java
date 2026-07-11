package de.as.fynancials.common.database.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.math.BigDecimal;

@Converter
public class BigDecimalConverter implements AttributeConverter<BigDecimal, String> {

  @Override
  public String convertToDatabaseColumn(BigDecimal bigDecimal) {
    if (bigDecimal == null) {
      return null;
    }
    return bigDecimal.toPlainString();
  }

  @Override
  public BigDecimal convertToEntityAttribute(String string) {
    if (string == null) {
      return null;
    }
    return new BigDecimal(string);
  }
}
