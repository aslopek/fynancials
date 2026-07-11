package de.as.fynancials.notification.dividendannouncement.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import integration.IntegrationTest;
import integration.sql.TestDataQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class DeleteDividendAnnouncementConfigTest {

  private static final String ENDPOINT = "/notifications/dividend-announcements/configs/%d";

  @Autowired
  private DividendAnnouncementConfigRepository dividendAnnouncementConfigRepository;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void deleteConfig_ok() throws Exception {
    long securityId = 3;
    long count = dividendAnnouncementConfigRepository.count();

    MvcResult mvcResult = deleteConfig(securityId).andExpect(status().isNoContent()).andReturn();

    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(dividendAnnouncementConfigRepository.count()).isEqualTo(count - 1);
    assertThat(dividendAnnouncementConfigRepository.existsBySecurityId(securityId)).isFalse();
    assertThat(TestDataQuery.getDividendAnnouncementConfigCountBySecurityId(securityId)).isZero();
  }

  @Test
  void deleteConfig_notFound() throws Exception {
    long securityId = 999;
    long count = dividendAnnouncementConfigRepository.count();

    MvcResult mvcResult = deleteConfig(securityId).andExpect(status().isNotFound()).andReturn();

    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(dividendAnnouncementConfigRepository.count()).isEqualTo(count);
  }

  private ResultActions deleteConfig(long securityId) throws Exception {
    String url = String.format(ENDPOINT, securityId);
    return mockMvc.perform(delete(url));
  }
}
