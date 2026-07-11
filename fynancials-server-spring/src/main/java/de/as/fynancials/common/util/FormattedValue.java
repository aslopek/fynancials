package de.as.fynancials.common.util;

import java.util.Set;
import lombok.Getter;

public class FormattedValue {

  private static final String MASK = "*****";

  @Getter
  private final String formattedValue;
  private final String maskedValue;

  FormattedValue(String formattedValue, Set<String> maskedValues) {
    String s = formattedValue;
    for (String maskedValue : maskedValues) {
      s = s.replace(maskedValue, MASK);
    }
    this.formattedValue = formattedValue;
    this.maskedValue = s;
  }

  @Override
  public String toString() {
    return this.maskedValue;
  }
}
