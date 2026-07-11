package de.as.fynancials.exchangerates;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "exchange-rate.update.enabled", havingValue = "true")
class ExchangeRateUpdater {

  private final ExchangeRateServiceImpl exchangeRateService;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final Clock clock;

  @EventListener(ApplicationReadyEvent.class)
  void updateExchangeRates() {
    log.info("Start updating exchange rates");
    exchangeRateService.updateExchangeRates();
    log.info("Updating exchange rates done");
    applicationEventPublisher.publishEvent(new ExchangeRateReadyEvent(this, clock));
  }
}
