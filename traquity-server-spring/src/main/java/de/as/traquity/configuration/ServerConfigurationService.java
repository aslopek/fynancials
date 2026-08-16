package de.as.traquity.configuration;

import java.util.Set;

public interface ServerConfigurationService {

  String getDefaultCurrency();

  Set<String> getSupportedCurrencies();
}
