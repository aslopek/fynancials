package de.as.fynancials.exchangerates;

import de.as.fynancials.configuration.ServerConfigurationService;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
class ExchangeRateConfig {

  @Bean
  Supplier<EcbExchangeRateFetcher> ecbExchangeRateFetcherSupplier(final RestTemplate restTemplate,
                                                                  final ServerConfigurationService serverConfigurationService) {
    return () -> new EcbExchangeRateFetcher(restTemplate, serverConfigurationService);
  }
}
