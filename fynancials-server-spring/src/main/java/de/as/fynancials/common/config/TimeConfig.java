package de.as.fynancials.common.config;

import java.math.BigDecimal;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfig {

  public static final BigDecimal DAYS_PER_YEAR = new BigDecimal("365.25");

  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone();
  }
}
