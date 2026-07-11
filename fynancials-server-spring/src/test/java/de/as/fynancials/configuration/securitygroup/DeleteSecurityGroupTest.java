package de.as.fynancials.configuration.securitygroup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import integration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class DeleteSecurityGroupTest {

  private static final String ENDPOINT = "/config/security-groups/%d";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private SecurityGroupRepository SecurityGroupRepository;

  @Test
  void deleteSecurityGroup_ok() throws Exception {
    long SecurityGroupCount = SecurityGroupRepository.count();
    MvcResult mvcResult = deleteSecurityGroup(2).andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(SecurityGroupRepository.count()).isEqualTo(SecurityGroupCount - 1);
    assertThat(SecurityGroupRepository.existsById(2L)).isFalse();
  }

  @Test
  void deleteSecurityGroup_groupDoesNotExist_notFound() throws Exception {
    long securityGroupCount = SecurityGroupRepository.count();
    MvcResult mvcResult = deleteSecurityGroup(999).andExpect(status().isNotFound()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(SecurityGroupRepository.count()).isEqualTo(securityGroupCount);
  }

  private ResultActions deleteSecurityGroup(long groupId) throws Exception {
    return mockMvc.perform(delete(String.format(ENDPOINT, groupId)));
  }
}