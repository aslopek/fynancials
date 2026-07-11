package de.as.fynancials.notification.dividendannouncement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.as.fynancials.notification.dividendannouncement.api.model.DividendAnnouncementUpdateDto;
import integration.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class UpdateDividendAnnouncementTest {

  private static final String ENDPOINT = "/notifications/dividend-announcements/%d";
  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private DividendAnnouncementRepository dividendAnnouncementRepository;

  private DividendAnnouncementUpdateDto requestBody;

  @BeforeEach
  void beforeEach() {
    requestBody = new DividendAnnouncementUpdateDto();
  }

  @Test
  void updateIsNew_false2false_ok() throws Exception {
    requestBody.setIsNew(false);
    long id = 1;
    MvcResult mvcResult = updateDividendAnnouncement(id).andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    DividendAnnouncementEntity entity = dividendAnnouncementRepository.findById(id).orElseThrow();
    assertThat(entity.isNew()).isFalse();
  }

  @Test
  void updateIsNew_false2true_badRequest() throws Exception {
    requestBody.setIsNew(true);
    long id = 1;
    MvcResult mvcResult = updateDividendAnnouncement(id).andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    DividendAnnouncementEntity entity = dividendAnnouncementRepository.findById(id).orElseThrow();
    assertThat(entity.isNew()).isFalse();
  }

  @Test
  void updateIsNew_true2false_ok() throws Exception {
    requestBody.setIsNew(false);
    long id = 4;
    MvcResult mvcResult = updateDividendAnnouncement(id).andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    DividendAnnouncementEntity entity = dividendAnnouncementRepository.findById(id).orElseThrow();
    assertThat(entity.isNew()).isFalse();
  }

  @Test
  void updateIsNew_true2true_badRequest() throws Exception {
    requestBody.setIsNew(true);
    long id = 4;
    MvcResult mvcResult = updateDividendAnnouncement(id).andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    DividendAnnouncementEntity entity = dividendAnnouncementRepository.findById(id).orElseThrow();
    assertThat(entity.isNew()).isTrue();
  }

  @Test
  void update_missingIsNew_badRequest() throws Exception {
    long id = 1;
    MvcResult mvcResult = updateDividendAnnouncement(id).andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    DividendAnnouncementEntity entity = dividendAnnouncementRepository.findById(id).orElseThrow();
    assertThat(entity.isNew()).isFalse();

    id = 4;
    mvcResult = updateDividendAnnouncement(id).andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    entity = dividendAnnouncementRepository.findById(id).orElseThrow();
    assertThat(entity.isNew()).isTrue();
  }

  @Test
  void update_idNotFound() throws Exception {
    requestBody.setIsNew(false);
    MvcResult mvcResult = updateDividendAnnouncement(999).andExpect(status().isNotFound()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  private ResultActions updateDividendAnnouncement(long id) throws Exception {
    String requestBodyAsString = objectMapper.writeValueAsString(requestBody);
    String url = String.format(ENDPOINT, id);
    return mockMvc.perform(put(url).content(requestBodyAsString).contentType(MediaType.APPLICATION_JSON));
  }
}
