package de.as.traquity.configuration;

import de.as.traquity.config.api.model.BackendServiceInfoDto;
import de.as.traquity.config.api.model.DatabaseConfigDto;
import java.util.List;
import java.util.Set;

public interface ServerConfigurationService {

  String getDefaultCurrency();

  Set<String> getSupportedCurrencies();

  DatabaseConfigDto getDatabaseConfig();

  boolean isDevModeActive();

  void setDevModeActive(boolean active);

  List<BackendServiceInfoDto> getBackendServiceInfo();
}
