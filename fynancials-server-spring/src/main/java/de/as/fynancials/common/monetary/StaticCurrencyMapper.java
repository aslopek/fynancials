package de.as.fynancials.common.monetary;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StaticCurrencyMapper implements BiConsumer<List<BigDecimal>, List<String>> {

  private final CurrencyMapping currencyMapping;
  private final MathContext mathContext;

  @Override
  public void accept(List<BigDecimal> monetaryValues, List<String> currencyKeys) {
    if (monetaryValues.size() != currencyKeys.size()) {
      throw new IllegalArgumentException(
          String.format("monetaryValues.size() != currencyKeys.size() (%d != %d))", monetaryValues.size(),
              currencyKeys.size()));
    }

    String currencyKey;
    String mappedCurrencyCode;
    BigDecimal multiplier;
    for (int i = 0; i < monetaryValues.size(); i++) {
      currencyKey = currencyKeys.get(i);
      mappedCurrencyCode = this.currencyMapping.getMappedCurrencyCode(currencyKeys.get(i));
      if (mappedCurrencyCode == null) {
        continue;
      }

      currencyKeys.set(i, mappedCurrencyCode);
      multiplier = this.currencyMapping.getMultiplier(currencyKey);
      if (multiplier != null) {
        multiplier = multiplier.multiply(monetaryValues.get(i), mathContext);
        monetaryValues.set(i, multiplier);
      }
    }
  }
}
