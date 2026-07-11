package de.as.fynancials.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import integration.IntegrationTest;
import integration.SecurityIds;
import integration.sql.TestDataQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class DeleteSecurityTest {

  private static final String ENDPOINT = "/securities/%d";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private SecurityRepository securityRepository;

  @Autowired
  private SecurityLogoRepository securityLogoRepository;

  @Test
  void deleteSecurity_success() throws Exception {
    long securityId = SecurityIds.MSFT;
    long count = securityRepository.count();
    MvcResult result = deleteSecurity(securityId).andExpect(status().isNoContent()).andReturn();
    assertThat(result.getResponse().getContentLength()).isZero();
    assertThat(securityRepository.count()).isEqualTo(count - 1);
    assertThat(securityRepository.existsById(securityId)).isFalse();
    assertThat(TestDataQuery.getDividendAnnouncementCountBySecurityId(securityId)).isZero();
    assertThat(TestDataQuery.getDividendAnnouncementConfigCountBySecurityId(securityId)).isZero();
  }

  @Test
  void deleteSecurity_notFound() throws Exception {
    long count = securityRepository.count();
    deleteSecurity(999).andExpect(status().isNotFound());
    assertThat(securityRepository.count()).isEqualTo(count);
  }

  @Test
  void deleteSecurity_logoIsAlsoDeleted() throws Exception {
    final long securityId = 8;
    // precondition
    assertThat(securityLogoRepository.existsById(securityId)).isTrue();

    long logoCount = securityLogoRepository.count();
    long securityCount = securityRepository.count();

    MvcResult result = deleteSecurity(securityId).andExpect(status().isNoContent()).andReturn();
    assertThat(result.getResponse().getContentLength()).isZero();

    assertThat(securityRepository.count()).isEqualTo(securityCount - 1);
    assertThat(securityRepository.existsById(securityId)).isFalse();
    assertThat(securityLogoRepository.count()).isEqualTo(logoCount - 1);
    assertThat(securityLogoRepository.existsById(securityId)).isFalse();
  }

  private ResultActions deleteSecurity(long id) throws Exception {
    return mockMvc.perform(delete(String.format(ENDPOINT, id)));
  }
}
