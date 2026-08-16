package de.as.traquity.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.as.traquity.admin.api.model.DatabaseConfigDto;
import integration.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@IntegrationTest
class GetDatabaseConfigTest {

  private static final String ENDPOINT = "/admin/database";

  @Autowired
  private MockMvc mockMvc;

  private ObjectMapper objectMapper;

  @BeforeEach
  void beforeEach() {
    objectMapper = new ObjectMapper().findAndRegisterModules();
  }

  @Test
  void getDatabaseConfig_ok() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get(ENDPOINT)).andExpect(status().isOk()).andReturn();
    DatabaseConfigDto databaseConfig =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), DatabaseConfigDto.class);

    assertThat(databaseConfig.getUsername()).isEqualTo("test-user");
    assertThat(databaseConfig.getPassword()).isEqualTo("test-password");
    assertThat(databaseConfig.getConnectionString()).isEqualTo("jdbc:h2:mem:traquity");
    assertThat(databaseConfig.getWebInterfaceUrl()).isNull();
    assertThat(databaseConfig.getFileLocation()).isNull();
    assertThat(mvcResult.getResponse().getHeader("Cache-Control")).isEqualTo("no-store");
  }
}
