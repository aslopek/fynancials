package de.as.traquity.admin;

import de.as.traquity.admin.api.model.DatabaseConfigDto;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class AdminServiceImpl implements AdminService {

  private static final String H2_MEMORY_PREFIX = "jdbc:h2:mem";
  private static final String H2_FILE_PREFIX = "jdbc:h2:file:";
  private static final String H2_FILE_TYPE = ".mv.db";

  private static final String DEV_MODE = "dev-mode.active";

  private final ServerConfigurationRepository serverConfigurationRepository;
  private final String h2ConsolePath;

  @Value("${spring.datasource.url:}")
  private String datasourceUrl;

  @Value("${spring.datasource.username:}")
  private String datasourceUsername;

  @Value("${spring.datasource.password:}")
  private String datasourcePassword;

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
}
