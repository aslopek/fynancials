package de.as.fynancials.configuration;

import de.as.fynancials.config.api.controller.ConfigApiDelegate;
import de.as.fynancials.config.api.model.BackendServiceInfoDto;
import de.as.fynancials.config.api.model.DatabaseConfigDto;
import de.as.fynancials.config.api.model.ThirdPartyLicenseDto;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
class ConfigController implements ConfigApiDelegate {

  private final ClientConfigurationService clientConfigurationService;
  private final ServerConfigurationService serverConfigurationService;
  private final ThirdPartyLicenseService thirdPartyLicenseService;

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
  public ResponseEntity<List<BackendServiceInfoDto>> getBackendServicesInfo() {
    return ResponseEntity.ok(serverConfigurationService.getBackendServiceInfo());
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

  @Override
  public ResponseEntity<DatabaseConfigDto> getDatabaseConfig() {
    return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(serverConfigurationService.getDatabaseConfig());
  }

  @Override
  public ResponseEntity<List<ThirdPartyLicenseDto>> getThirdPartyLicenses() {
    return ResponseEntity.ok(thirdPartyLicenseService.getThirdPartyLicenses());
  }

  @Override
  public ResponseEntity<String> getPid() {
    long pid = ProcessHandle.current().pid();
    return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(Long.toString(pid));
  }

  @Override
  public ResponseEntity<String> isDevModeActive() {
    String responseBody = Boolean.toString(serverConfigurationService.isDevModeActive());
    return ResponseEntity.ok(responseBody);
  }

  @Override
  public ResponseEntity<Void> setDevModeActive(String body) {
    serverConfigurationService.setDevModeActive(Boolean.parseBoolean(body));
    return ResponseEntity.noContent().build();
  }
}
