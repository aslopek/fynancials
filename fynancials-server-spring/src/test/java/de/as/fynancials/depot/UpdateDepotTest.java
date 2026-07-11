package de.as.fynancials.depot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.as.fynancials.depot.api.model.DepotReadDto;
import de.as.fynancials.depot.api.model.DepotUpdateDto;
import integration.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class UpdateDepotTest {

  private static final String ENDPOINT = "/depots/%d";
  private static final String LOGO_ENDPOINT = ENDPOINT + "/logo";

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private DepotRepository depotRepository;


  private DepotUpdateDto requestBody;

  @BeforeEach
  void beforeEach() {
    requestBody = new DepotUpdateDto();
    requestBody.setName("Updated Depot Name");
    requestBody.setCurrency("EUR");
    requestBody.setVersion(0L);
  }

  @Test
  void updateDepot_name_ok() throws Exception {
    MvcResult mvcResult = putDepot(1L).andExpect(status().isOk()).andReturn();

    DepotReadDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), DepotReadDto.class);
    assertThat(responseBody.getId()).isEqualTo(1L);
    assertThat(responseBody.getName()).isEqualTo(requestBody.getName());
    assertThat(responseBody.getCurrency()).isEqualTo(requestBody.getCurrency());
    assertThat(responseBody.getVersion()).isEqualTo(requestBody.getVersion() + 1);
    verifyLogoLink(responseBody, true);

    DepotEntity depotEntity = depotRepository.findById(1L).orElseThrow();
    assertThat(depotEntity.getName()).isEqualTo(requestBody.getName());
    assertThat(depotEntity.getCurrency()).isEqualTo(requestBody.getCurrency());
    assertThat(depotEntity.getVersion()).isEqualTo(requestBody.getVersion() + 1);
  }

  @Test
  void updateDepot_trimmedName_ok() throws Exception {
    requestBody.setName("  \tUpdated Depot Name ");
    MvcResult mvcResult = putDepot(1L).andExpect(status().isOk()).andReturn();

    DepotReadDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), DepotReadDto.class);
    assertThat(responseBody.getId()).isEqualTo(1L);
    assertThat(responseBody.getName()).isEqualTo("Updated Depot Name");
    assertThat(responseBody.getCurrency()).isEqualTo(requestBody.getCurrency());
    assertThat(responseBody.getVersion()).isEqualTo(requestBody.getVersion() + 1);
    verifyLogoLink(responseBody, true);

    DepotEntity depotEntity = depotRepository.findById(1L).orElseThrow();
    assertThat(depotEntity.getName()).isEqualTo("Updated Depot Name");
    assertThat(depotEntity.getCurrency()).isEqualTo(requestBody.getCurrency());
    assertThat(depotEntity.getVersion()).isEqualTo(requestBody.getVersion() + 1);
  }

  @Test
  void updateDepot_currency_badRequest() throws Exception {
    requestBody.setCurrency("USD");
    MvcResult mvcResult = putDepot(1L).andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();

    DepotEntity depotEntity = depotRepository.findById(1L).orElseThrow();
    assertThat(depotEntity.getCurrency()).isEqualTo("EUR");
    assertThat(depotEntity.getVersion()).isEqualTo(0);
  }

  @Test
  void updateDepot_notFound() throws Exception {
    long depotCount = depotRepository.count();
    MvcResult mvcResult = putDepot(999L).andExpect(status().isNotFound()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(depotRepository.count()).isEqualTo(depotCount);
  }

  @Test
  void updateDepot_duplicateName_conflict() throws Exception {
    requestBody.setName("My other depot");
    MvcResult mvcResult = putDepot(1L).andExpect(status().isConflict()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();

    DepotEntity depotEntity = depotRepository.findById(1L).orElseThrow();
    assertThat(depotEntity.getName()).isEqualTo("My first depot");
    assertThat(depotEntity.getVersion()).isEqualTo(0);
  }

  @Test
  void updateDepot_wrongVersion_conflict() throws Exception {
    MvcResult mvcResult = putDepot(2L).andExpect(status().isConflict()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();

    DepotEntity depotEntity = depotRepository.findById(2L).orElseThrow();
    assertThat(depotEntity.getName()).isEqualTo("My other depot");
    assertThat(depotEntity.getVersion()).isEqualTo(1);
  }

  private ResultActions putDepot(long id) throws Exception {
    return mockMvc.perform(put(String.format(ENDPOINT, id)).contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestBody)));
  }

  private void verifyLogoLink(DepotReadDto responseBody, boolean shouldHaveLogo) {
    assertThat(responseBody.getLinks()).isNotNull();
    if (shouldHaveLogo) {
      assertThat(responseBody.getLinks().getLogo()).isEqualTo(String.format(LOGO_ENDPOINT, responseBody.getId()));
    } else {
      assertThat(responseBody.getLinks().getLogo()).isNull();
    }
  }
}
