package de.as.fynancials.configuration;

import de.as.fynancials.common.error.NotFoundException;
import java.util.Map;

interface ClientConfigurationService {

  void deleteConfigValues(String clientId);

  Map<String, String> getConfigValues(String clientId, String prefix);

  String getConfigValue(String clientId, String configKey) throws NotFoundException;

  void setConfigValue(String clientId, String configKey, String configValue);

  void deleteConfigValue(String clientId, String configKey);
}
