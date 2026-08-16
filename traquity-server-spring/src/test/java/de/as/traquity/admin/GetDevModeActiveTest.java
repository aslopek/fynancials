package de.as.traquity.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import integration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@IntegrationTest
class GetDevModeActiveTest {

  private static final String ENDPOINT = "/admin/dev-mode";
  private static final String DEV_MODE = "dev-mode.active";

  @Autowired
  private ServerConfigurationRepository serverConfigurationRepository;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void getDevModeActive_true_ok() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get(ENDPOINT)).andExpect(status().isOk()).andReturn();
    assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
    assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("true");
  }

  @Test
  void getDevModeActive_false_ok() throws Exception {
    // arrange
    ServerConfigurationEntity entity = serverConfigurationRepository.findByConfigKey(DEV_MODE).orElseThrow();
    entity.setConfigValue("false");
    serverConfigurationRepository.saveAndFlush(entity);

    // act
    MvcResult mvcResult = mockMvc.perform(get(ENDPOINT)).andExpect(status().isOk()).andReturn();

    // assert
    assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
    assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("false");
  }

  @Test
  void getDevModeActive_notPresent_ok() throws Exception {
    // arrange
    serverConfigurationRepository.delete(serverConfigurationRepository.findByConfigKey(DEV_MODE).orElseThrow());

    // act
    MvcResult mvcResult = mockMvc.perform(get(ENDPOINT)).andExpect(status().isOk()).andReturn();

    // assert
    assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
    assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("false");
  }
}
