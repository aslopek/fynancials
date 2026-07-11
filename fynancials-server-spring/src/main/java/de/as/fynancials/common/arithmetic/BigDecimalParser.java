package de.as.fynancials.common.arithmetic;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BigDecimalParser implements Function<Object, BigDecimal> {

  private static final Pattern PATTERN = Pattern.compile("[-+]?[0-9]*[.,]?[0-9]+([eE][-+]?[0-9]+)?");

  @Override
  public BigDecimal apply(Object value) {
    if (value == null) {
      return BigDecimal.ZERO;
    }

    return switch (value) {
      case Double d -> BigDecimal.valueOf(d);
      case Integer i -> BigDecimal.valueOf(i);
      case BigDecimal bigDecimal -> bigDecimal;
      case String string -> parse(string);
      default -> {
        log.error("Could not parse (value = {}, type = {})", value, value.getClass().getName());
        yield BigDecimal.ZERO;
      }
    };
  }

  private BigDecimal parse(String value) {
    if (value == null || value.isBlank()) {
      return BigDecimal.ZERO;
    }

    Matcher matcher = PATTERN.matcher(value);
    if (matcher.find()) {
      return new BigDecimal(matcher.group().replace(",", "."));
    }
    log.error("Could not parse value {}", value);
    return BigDecimal.ZERO;
  }
}
