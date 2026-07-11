package de.as.fynancials.price.security.historical;

import de.as.fynancials.exchangerates.ExchangeRateReadyEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "historical-security-price.update.enabled", havingValue = "true")
class HistoricalSecurityPriceUpdater {

  private final HistoricalSecurityPriceServiceImpl historicalSecurityPriceService;

  @EventListener(ExchangeRateReadyEvent.class)
  void updateHistoricalPrices() {
    log.info("Start updating historical prices");
    List<HistoricalSecurityPriceConfig> configs = historicalSecurityPriceService.getActiveConfigs();
    for (HistoricalSecurityPriceConfig config : configs) {
      try {
        historicalSecurityPriceService.updatePrices(config.getSecurityId());
      } catch (RuntimeException e) {
        log.warn("Failed to update historical prices for security {}", config.getSecurityId(), e);
      }
    }
    log.info("Updating historical prices done");
  }
}
