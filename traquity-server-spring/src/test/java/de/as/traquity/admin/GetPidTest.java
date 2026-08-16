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
class GetPidTest {

  private static final String ENDPOINT = "/admin/pid";

  @Autowired
  private MockMvc mockMvc;

  @Test
  void getPid_ok() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get(ENDPOINT)).andExpect(status().isOk()).andReturn();
    assertThat(mvcResult.getResponse().getContentType()).isEqualTo("application/json");
    assertThat(mvcResult.getResponse().getContentAsString()).matches("^[1-9][0-9]*$");
  }
}
