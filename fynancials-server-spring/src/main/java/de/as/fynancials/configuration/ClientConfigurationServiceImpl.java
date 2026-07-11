package de.as.fynancials.configuration;

import de.as.fynancials.common.error.NotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class ClientConfigurationServiceImpl implements ClientConfigurationService {

  private final ClientConfigurationRepository clientConfigurationRepository;

  @Override
  public void deleteConfigValues(String clientId) {
    clientConfigurationRepository.deleteAllByClientId(clientId);
  }

  @Override
  public Map<String, String> getConfigValues(String clientId, String prefix) {
    List<ClientConfigurationEntity> allConfigValues;

    if (prefix == null || prefix.isBlank()) {
      allConfigValues = clientConfigurationRepository.findAllByClientId(clientId);
    } else {
      allConfigValues = clientConfigurationRepository.findAllByClientIdAndConfigKeyStartsWith(clientId, prefix);
    }

    Map<String, String> result = new HashMap<>(allConfigValues.size());
    for (ClientConfigurationEntity clientConfigurationEntity : allConfigValues) {
      result.put(clientConfigurationEntity.getConfigKey(), clientConfigurationEntity.getConfigValue());
    }
    return result;
  }

  @Override
  public String getConfigValue(String clientId, String configKey) throws NotFoundException {
    return clientConfigurationRepository.findByClientIdAndConfigKey(clientId, configKey)
        .orElseThrow(NotFoundException::new).getConfigValue();
  }

  @Override
  public void setConfigValue(String clientId, String configKey, String configValue) {
    Optional<ClientConfigurationEntity> existingConfigValue =
        clientConfigurationRepository.findByClientIdAndConfigKey(clientId, configKey);

    if (existingConfigValue.isPresent()) {
      existingConfigValue.get().setConfigValue(configValue);
      clientConfigurationRepository.saveAndFlush(existingConfigValue.get());
    } else {
      ClientConfigurationEntity newConfigValue = new ClientConfigurationEntity();
      newConfigValue.setClientId(clientId);
      newConfigValue.setConfigKey(configKey);
      newConfigValue.setConfigValue(configValue);
      clientConfigurationRepository.saveAndFlush(newConfigValue);
    }
  }

  @Override
  public void deleteConfigValue(String clientId, String configKey) {
    clientConfigurationRepository.deleteByClientIdAndConfigKey(clientId, configKey);
  }
}
