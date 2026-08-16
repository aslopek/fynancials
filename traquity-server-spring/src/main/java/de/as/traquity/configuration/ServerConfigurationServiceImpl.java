package de.as.traquity.configuration;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class ServerConfigurationServiceImpl implements ServerConfigurationService {

  private static final String DEFAULT_CURRENCY = "EUR";
  private static final Set<String> SUPPORTED_CURRENCIES =
      Set.of("EUR", "USD", "JPY", "DKK", "GBP", "PLN", "SEK", "CHF", "NOK", "AUD", "CAD", "CNY", "HKD", "ILS", "BRL");

  @Override
  public String getDefaultCurrency() {
    return DEFAULT_CURRENCY;
  }

  @Override
  public Set<String> getSupportedCurrencies() {
    return SUPPORTED_CURRENCIES;
  }
}
