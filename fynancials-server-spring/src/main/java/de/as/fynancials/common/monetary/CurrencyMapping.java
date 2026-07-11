package de.as.fynancials.common.monetary;

import java.math.BigDecimal;

public interface CurrencyMapping {

  /**
   * Gets the mapped currency code.
   *
   * @param currencyKey key for retrieving the mapped currency code
   * @return the mapped currency code or {@code null}, if no mapping exists
   */
  String getMappedCurrencyCode(String currencyKey);

  /**
   * Gets the multiplier for the currency code.
   *
   * @param currencyKey for retrieving the multiplier
   * @return the multiplier or {@code null}, if no multiplier exists
   */
  BigDecimal getMultiplier(String currencyKey);
}
