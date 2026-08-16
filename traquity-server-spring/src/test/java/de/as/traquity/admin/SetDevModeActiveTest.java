package de.as.traquity.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import integration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
class SetDevModeActiveTest {

  private static final String ENDPOINT = "/admin/dev-mode";
  private static final String DEV_MODE = "dev-mode.active";

  @Autowired
  private ServerConfigurationRepository serverConfigurationRepository;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void setDevMode_deactivate_ok() throws Exception {
    // act
    mockMvc.perform(put(ENDPOINT).content("false").contentType(MediaType.APPLICATION_JSON))
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
    mockMvc.perform(put(ENDPOINT).content("true").contentType(MediaType.APPLICATION_JSON))
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
    mockMvc.perform(put(ENDPOINT).content("true").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    // assert
    assertThat(serverConfigurationRepository.findByConfigKey(DEV_MODE).orElseThrow().getConfigValue()).isEqualTo(
        "true");
  }

  @Test
  void setDevMode_invalidInput_badRequest() throws Exception {
    mockMvc.perform(put(ENDPOINT).content("invalid").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    assertThat(serverConfigurationRepository.findByConfigKey(DEV_MODE).orElseThrow().getConfigValue()).isEqualTo(
        "true");
  }
}
