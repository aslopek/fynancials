package de.as.traquity.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import integration.IntegrationTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@IntegrationTest
class ServerConfigurationTest {

  @Autowired
  private MockMvc mockMvc;

  private ObjectMapper objectMapper;

  @BeforeEach
  void beforeEach() {
    objectMapper = new ObjectMapper().findAndRegisterModules();
  }

  @Test
  void getDefaultCurrency() throws Exception {
    MvcResult mvcResult =
        mockMvc.perform(get("/config/currencies/default")).andExpect(status().isOk()).andReturn();
    assertThat(mvcResult.getResponse().getContentType()).isEqualTo("text/plain");
    assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("EUR");
  }

  @Test
  void getSupportedCurrencies() throws Exception {
    MvcResult mvcResult =
        mockMvc.perform(get("/config/currencies")).andExpect(status().isOk()).andReturn();
    assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");

    List<String> supportedCurrencies =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});
    assertThat(supportedCurrencies).containsExactlyInAnyOrder("EUR", "USD", "JPY", "DKK", "GBP", "PLN", "SEK", "CHF",
        "NOK", "AUD", "CAD", "CNY", "HKD", "ILS", "BRL");
  }
}
