package de.as.traquity.configuration;

import de.as.traquity.config.api.controller.ConfigApiDelegate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
class ConfigController implements ConfigApiDelegate {

  private final ClientConfigurationService clientConfigurationService;
  private final ServerConfigurationService serverConfigurationService;

  @Override
  public ResponseEntity<Void> deleteClientConfig(String clientId) {
    clientConfigurationService.deleteConfigValues(clientId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> deleteClientConfigValue(String clientId, String clientConfigKey) {
    clientConfigurationService.deleteConfigValue(clientId, clientConfigKey);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Map<String, String>> getClientConfig(String clientId, String prefix) {
    return ResponseEntity.ok(clientConfigurationService.getConfigValues(clientId, prefix));
  }

  @Override
  public ResponseEntity<String> getClientConfigValue(String clientId, String clientConfigKey) {
    return ResponseEntity.ok(clientConfigurationService.getConfigValue(clientId, clientConfigKey));
  }

  @Override
  public ResponseEntity<Void> setClientConfigValue(String clientId, String clientConfigKey, String body) {
    clientConfigurationService.setConfigValue(clientId, clientConfigKey, body);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<String> getDefaultCurrency() {
    return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(serverConfigurationService.getDefaultCurrency());
  }

  @Override
  public ResponseEntity<List<String>> getSupportedCurrencies() {
    Set<String> supportedCurrencies = serverConfigurationService.getSupportedCurrencies();
    return ResponseEntity.ok(List.copyOf(supportedCurrencies));
  }
}
