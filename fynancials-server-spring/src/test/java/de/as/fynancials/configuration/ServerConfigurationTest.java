package de.as.fynancials.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.as.fynancials.config.api.model.DatabaseConfigDto;
import integration.IntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@IntegrationTest
class ServerConfigurationTest {

  private static final String DEV_MODE = "dev-mode.active";

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Autowired
  private ServerConfigurationRepository serverConfigurationRepository;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void getDefaultCurrency() throws Exception {
    MvcResult mvcResult =
        mockMvc.perform(get("/config/currency/default-currency")).andExpect(status().isOk()).andReturn();
    assertThat(mvcResult.getResponse().getContentType()).isEqualTo("text/plain");
    assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("EUR");
  }

  @Test
  void getSupportedCurrencies() throws Exception {
    MvcResult mvcResult =
        mockMvc.perform(get("/config/currency/supported-currencies")).andExpect(status().isOk()).andReturn();
    assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");

    List<String> supportedCurrencies =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});
    assertThat(supportedCurrencies).containsExactlyInAnyOrder("EUR", "USD", "JPY", "DKK", "GBP", "PLN", "SEK", "CHF",
        "NOK", "AUD", "CAD", "CNY", "HKD", "ILS", "BRL");
  }

  @Test
  void getDatabaseConfig_ok() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get("/config/database")).andExpect(status().isOk()).andReturn();
    DatabaseConfigDto databaseConfig =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), DatabaseConfigDto.class);

    assertThat(databaseConfig.getUsername()).isEqualTo("test-user");
    assertThat(databaseConfig.getPassword()).isEqualTo("test-password");
    assertThat(databaseConfig.getConnectionString()).isEqualTo("jdbc:h2:mem:fynancials");
    assertThat(databaseConfig.getWebInterfaceUrl()).isNull();
    assertThat(databaseConfig.getFileLocation()).isNull();
    assertThat(mvcResult.getResponse().getHeader("Cache-Control")).isEqualTo("no-store");
  }

  @Test
  void isDevModeActive_true_ok() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get("/config/dev-mode")).andExpect(status().isOk()).andReturn();
    assertThat(mvcResult.getResponse().getContentType()).isEqualTo("text/plain;charset=UTF-8");
    assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("true");
  }

  @Test
  void isDevModeActive_false_ok() throws Exception {
    // arrange
    ServerConfigurationEntity entity = serverConfigurationRepository.findByConfigKey(DEV_MODE).orElseThrow();
    entity.setConfigValue("false");
    serverConfigurationRepository.saveAndFlush(entity);

    // act
    MvcResult mvcResult = mockMvc.perform(get("/config/dev-mode")).andExpect(status().isOk()).andReturn();

    // assert
    assertThat(mvcResult.getResponse().getContentType()).isEqualTo("text/plain;charset=UTF-8");
    assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("false");
  }

  @Test
  void isDevModeActive_notPresent_ok() throws Exception {
    // arrange
    serverConfigurationRepository.delete(serverConfigurationRepository.findByConfigKey(DEV_MODE).orElseThrow());

    // act
    MvcResult mvcResult = mockMvc.perform(get("/config/dev-mode")).andExpect(status().isOk()).andReturn();

    // assert
    assertThat(mvcResult.getResponse().getContentType()).isEqualTo("text/plain;charset=UTF-8");
    assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("false");
  }

  @Test
  void setDevMode_deactivate_ok() throws Exception {
    // act
    mockMvc.perform(put("/config/dev-mode").content("false").contentType(MediaType.TEXT_PLAIN))
        .andExpect(status().isNoContent());

    // assert
    assertThat(serverConfigurationRepository.findByConfigKey(DEV_MODE).orElseThrow().getConfigValue()).isEqualTo(
        "false");
  }

  @Test
  void setDevMode_activate_exists_ok() throws Exception {
    // arrange
    ServerConfigurationEntity entity = serverConfigurationRepository.findByConfigKey(DEV_MODE).orElseThrow();
    entity.setConfigValue("false");
    serverConfigurationRepository.saveAndFlush(entity);

    // act
    mockMvc.perform(put("/config/dev-mode").content("true").contentType(MediaType.TEXT_PLAIN))
        .andExpect(status().isNoContent());

    // assert
    assertThat(serverConfigurationRepository.findByConfigKey(DEV_MODE).orElseThrow().getConfigValue()).isEqualTo(
        "true");
  }

  @Test
  void setDevMode_active_doesNotExist_ok() throws Exception {
    // arrange
    serverConfigurationRepository.delete(serverConfigurationRepository.findByConfigKey(DEV_MODE).orElseThrow());

    // act
    mockMvc.perform(put("/config/dev-mode").content("true").contentType(MediaType.TEXT_PLAIN))
        .andExpect(status().isNoContent());

    // assert
    assertThat(serverConfigurationRepository.findByConfigKey(DEV_MODE).orElseThrow().getConfigValue()).isEqualTo(
        "true");
  }

  @Test
  void setDevMode_invalidInput_badRequest() throws Exception {
    mockMvc.perform(put("/config/dev-mode").content("invalid").contentType(MediaType.TEXT_PLAIN))
        .andExpect(status().isBadRequest());

    assertThat(serverConfigurationRepository.findByConfigKey(DEV_MODE).orElseThrow().getConfigValue()).isEqualTo(
        "true");
  }

  @Test
  void getPid_ok() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get("/config/pid")).andExpect(status().isOk()).andReturn();
    assertThat(mvcResult.getResponse().getContentType()).isEqualTo("text/plain");
    assertThat(mvcResult.getResponse().getContentAsString()).matches("^[1-9][0-9]*$");
  }
}
