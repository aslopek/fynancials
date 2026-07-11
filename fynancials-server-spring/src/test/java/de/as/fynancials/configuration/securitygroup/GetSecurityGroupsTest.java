package de.as.fynancials.configuration.securitygroup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.as.fynancials.config.securitygroup.api.model.SecurityGroupReadDto;
import integration.IntegrationTest;
import integration.SecurityIds;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@IntegrationTest
class GetSecurityGroupsTest {

  private static final String ENDPONIT = "/config/security-groups";

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private SecurityGroupRepository securityGroupRepository;

  @Test
  void getSecurityGroups_ok() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get(ENDPONIT)).andExpect(status().isOk()).andReturn();
    List<SecurityGroupReadDto> responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

    assertThat(responseBody).hasSize(2);
    SecurityGroupReadDto expected = new SecurityGroupReadDto();
    expected.setId(1L);
    expected.setName("Volkswagen");
    expected.setSecurities(
        List.of(SecurityIds.VW_VZ, SecurityIds.VW_STAMM, SecurityIds.VW_VZ_ADR, SecurityIds.VW_STAMM_ADR));
    expected.setVersion(1L);
    assertThat(responseBody.get(0)).isEqualTo(expected);

    expected = new SecurityGroupReadDto();
    expected.setId(2L);
    expected.setName("Alphabet");
    expected.setSecurities(List.of(SecurityIds.GOOGL, SecurityIds.GOOG));
  }

  @Test
  void getSecurityGroups_empty_ok() throws Exception {
    securityGroupRepository.deleteAll();
    MvcResult mvcResult = mockMvc.perform(get(ENDPONIT)).andExpect(status().isOk()).andReturn();
    List<SecurityGroupReadDto> responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

    assertThat(responseBody).isNotNull();
    assertThat(responseBody).isEmpty();
  }
}
