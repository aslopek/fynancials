package de.as.fynancials.common.config;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.function.Function;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ArithmeticConfig {

  @Bean
  public MathContext mathContext() {
    return new MathContext(34, RoundingMode.HALF_UP);
  }

  @Bean
  public Function<Double, BigDecimal> bigDecimalParser() {
    return number -> number == null ? null : BigDecimal.valueOf(number);
  }
}
