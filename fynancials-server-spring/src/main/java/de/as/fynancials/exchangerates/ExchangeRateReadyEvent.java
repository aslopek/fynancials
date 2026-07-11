package de.as.fynancials.exchangerates;

import java.time.Clock;
import org.springframework.context.ApplicationEvent;

public class ExchangeRateReadyEvent extends ApplicationEvent {

  public ExchangeRateReadyEvent(Object source, Clock clock) {
    super(source, clock);
  }
}
