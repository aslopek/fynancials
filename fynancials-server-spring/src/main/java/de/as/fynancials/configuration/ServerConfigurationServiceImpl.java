package de.as.fynancials.configuration;

import de.as.fynancials.common.error.InternalServerErrorException;
import de.as.fynancials.config.api.model.BackendServiceInfoDto;
import de.as.fynancials.config.api.model.DatabaseConfigDto;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class ServerConfigurationServiceImpl implements ServerConfigurationService {

  private static final String POM_PROPERTIES = "properties-from-pom.properties";
  private static final String H2_MEMORY_PREFIX = "jdbc:h2:mem";
  private static final String H2_FILE_PREFIX = "jdbc:h2:file:";
  private static final String H2_FILE_TYPE = ".mv.db";

  private static final String DEV_MODE = "dev-mode.active";

  private static final String DEFAULT_CURRENCY = "EUR";
  private static final Set<String> SUPPORTED_CURRENCIES =
      Set.of("EUR", "USD", "JPY", "DKK", "GBP", "PLN", "SEK", "CHF", "NOK", "AUD", "CAD", "CNY", "HKD", "ILS", "BRL");


  private final ServerConfigurationRepository serverConfigurationRepository;
  private final String h2ConsolePath;

  @Value("${spring.datasource.url:}")
  private String datasourceUrl;

  @Value("${spring.datasource.username:}")
  private String datasourceUsername;

  @Value("${spring.datasource.password:}")
  private String datasourcePassword;

  @Override
  public String getDefaultCurrency() {
    return DEFAULT_CURRENCY;
  }

  @Override
  public Set<String> getSupportedCurrencies() {
    return SUPPORTED_CURRENCIES;
  }

  @Override
  public DatabaseConfigDto getDatabaseConfig() {
    DatabaseConfigDto databaseConfigDto = new DatabaseConfigDto();
    databaseConfigDto.setUsername(datasourceUsername);
    databaseConfigDto.setPassword(this.isDevModeActive() ? datasourcePassword : "");
    databaseConfigDto.setConnectionString(datasourceUrl);

    if (h2ConsolePath != null && !h2ConsolePath.isBlank()) {
      databaseConfigDto.setWebInterfaceUrl(h2ConsolePath);
    }

    if (datasourceUrl.startsWith(H2_FILE_PREFIX) && !datasourceUrl.startsWith(H2_MEMORY_PREFIX)) {
      String home = System.getProperty("user.home").replaceAll("\\\\", "/");
      String path = datasourceUrl.replace(H2_FILE_PREFIX, "").replaceFirst("^~", home);
      path = path.split(";")[0] + H2_FILE_TYPE;
      databaseConfigDto.setFileLocation(path);
    }

    return databaseConfigDto;
  }

  @Override
  public boolean isDevModeActive() {
    Optional<ServerConfigurationEntity> devMode = serverConfigurationRepository.findByConfigKey(DEV_MODE);
    if (devMode.isEmpty()) {
      return false;
    }

    String value = devMode.get().getConfigValue();
    if (value == null) {
      return false;
    }
    return "true".equalsIgnoreCase(value);
  }

  @Override
  public void setDevModeActive(boolean active) {
    Optional<ServerConfigurationEntity> devMode = serverConfigurationRepository.findByConfigKey(DEV_MODE);
    if (devMode.isEmpty()) {
      ServerConfigurationEntity entity = new ServerConfigurationEntity();
      entity.setConfigKey(DEV_MODE);
      entity.setConfigValue(Boolean.toString(active));
      serverConfigurationRepository.saveAndFlush(entity);
    } else {
      devMode.get().setConfigValue(Boolean.toString(active));
      serverConfigurationRepository.saveAndFlush(devMode.get());
    }
  }

  @Override
  public List<BackendServiceInfoDto> getBackendServiceInfo() {
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(POM_PROPERTIES)) {
      Properties properties = new Properties();
      properties.load(inputStream);

      BackendServiceInfoDto info = new BackendServiceInfoDto();
      info.setName("Fynancials Server Spring");
      info.setVersion(properties.getProperty("application.version"));
      info.setImplementedApis(
          List.of("configuration", "configuration-security-group", "depot", "depot-dividend", "depot-performance",
              "depot-position", "depot-transaction", "historical-security-price", "notification-dividend-announcement", "security"));
      return List.of(info);
    } catch (IOException e) {
      throw new InternalServerErrorException();
    }
  }
}
