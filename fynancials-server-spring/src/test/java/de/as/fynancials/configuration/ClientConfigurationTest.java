package de.as.fynancials.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import integration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@IntegrationTest
class ClientConfigurationTest {

  private static final String ENDPOINT_CLIENT_CONFIG = "/config/clients/%s";
  private static final String ENDPOINT_CLIENT_CONFIG_VALUE = "/config/clients/%s/%s";

  @Autowired
  private ClientConfigurationRepository clientConfigurationRepository;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void getClientConfig_ok() throws Exception {
    mockMvc.perform(get(String.format(ENDPOINT_CLIENT_CONFIG, "some-client"))).andExpect(status().isOk())
        .andExpect(content().json("{\"some-key\":\"123\",\"some-other-key\":\"abc\"}"))
        .andExpect(content().contentType("application/json"));
  }

  @Test
  void getClientConfig_prefix_ok() throws Exception {
    String endpoint = String.format(ENDPOINT_CLIENT_CONFIG, "prefix-client") + "?prefix=prefix1";
    mockMvc.perform(get(endpoint)).andExpect(status().isOk())
        .andExpect(content().json("{\"prefix1.foo\":\"abc\",\"prefix1.bar\":\"def\"}"))
        .andExpect(content().contentType("application/json"));
  }

  @Test
  void deleteClientConfig_ok() throws Exception {
    long count = clientConfigurationRepository.count();
    MvcResult mvcResult =
        mockMvc.perform(delete(String.format(ENDPOINT_CLIENT_CONFIG, "some-client"))).andExpect(status().isNoContent())
            .andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(clientConfigurationRepository.count()).isEqualTo(count - 2);
    assertThat(clientConfigurationRepository.findAllByClientId("some-other-client")).hasSize(1);
  }

  @Test
  void getClientConfigValue_ok() throws Exception {
    mockMvc.perform(get(String.format(ENDPOINT_CLIENT_CONFIG_VALUE, "some-client", "some-key")))
        .andExpect(status().isOk()).andExpect(content().string("123"))
        .andExpect(content().contentType("text/plain;charset=UTF-8"));
  }

  @Test
  void getClientConfigValue_notFound() throws Exception {
    MvcResult mvcResult =
        mockMvc.perform(get(String.format(ENDPOINT_CLIENT_CONFIG_VALUE, "some-client", "unknown-key")))
            .andExpect(status().isNotFound()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void setClientConfigValue_addNew_ok() throws Exception {
    MvcResult mvcResult = mockMvc.perform(
        put(String.format(ENDPOINT_CLIENT_CONFIG_VALUE, "some-client", "unknown-key")).content("new-value")
            .contentType(MediaType.TEXT_PLAIN)).andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();

    ClientConfigurationEntity entity =
        clientConfigurationRepository.findByClientIdAndConfigKey("some-client", "unknown-key").orElseThrow();
    assertThat(entity.getConfigValue()).isEqualTo("new-value");
  }

  @Test
  void setClientConfigValue_updateExisting_ok() throws Exception {
    MvcResult mvcResult = mockMvc.perform(
        put(String.format(ENDPOINT_CLIENT_CONFIG_VALUE, "some-client", "some-key")).content("new-value")
            .contentType(MediaType.TEXT_PLAIN)).andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();

    ClientConfigurationEntity entity =
        clientConfigurationRepository.findByClientIdAndConfigKey("some-client", "some-key").orElseThrow();
    assertThat(entity.getConfigValue()).isEqualTo("new-value");
  }

  @Test
  void deleteClientConfigValue_exists_ok() throws Exception {
    long count = clientConfigurationRepository.count();
    MvcResult mvcResult =
        mockMvc.perform(delete(String.format(ENDPOINT_CLIENT_CONFIG_VALUE, "some-client", "some-key")))
            .andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(clientConfigurationRepository.count()).isEqualTo(count - 1);
    assertThat(clientConfigurationRepository.findByClientIdAndConfigKey("some-client", "some-key")).isEmpty();
  }

  @Test
  void deleteClientConfigValue_doesNotExist_ok() throws Exception {
    long count = clientConfigurationRepository.count();
    MvcResult mvcResult =
        mockMvc.perform(delete(String.format(ENDPOINT_CLIENT_CONFIG_VALUE, "some-client", "unknown-key")))
            .andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(clientConfigurationRepository.count()).isEqualTo(count);
  }
}
