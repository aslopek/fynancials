package de.as.fynancials.depot;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.as.fynancials.depot.api.model.DepotReadDto;
import integration.IntegrationTest;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class GetDepotTest {

  private static final String ENDPOINT = "/depots/%d";
  private static final String LOGO_ENDPOINT = ENDPOINT + "/logo";

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Autowired
  private MockMvc mockMvc;

  @Test
  void getDepot_empty_ok() throws Exception {
    MvcResult mvcResult = getDepot(3L).andExpect(status().isOk()).andReturn();
    DepotReadDto depot = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), DepotReadDto.class);
    assertThat(depot.getId()).isEqualTo(3);
    assertThat(depot.getVersion()).isEqualTo(0);
    assertThat(depot.getName()).isEqualTo("Empty depot");
    assertThat(depot.getCurrency()).isEqualTo("EUR");
    verifyLogoLink(depot, true);
  }

  @Test
  void getDepot_notFound() throws Exception {
    MvcResult mvcResult = getDepot(999L).andExpect(status().isNotFound()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void getDepot_firstDepot_ok() throws Exception {
    MvcResult mvcResult = getDepot(1L).andExpect(status().isOk()).andReturn();
    DepotReadDto depot = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), DepotReadDto.class);
    assertThat(depot.getId()).isEqualTo(1);
    assertThat(depot.getVersion()).isEqualTo(0);
    assertThat(depot.getName()).isEqualTo("My first depot");
    assertThat(depot.getCurrency()).isEqualTo("EUR");
    verifyLogoLink(depot, true);

  }

  @Test
  void getDepot_otherDepot_ok() throws Exception {
    MvcResult mvcResult = getDepot(2L).andExpect(status().isOk()).andReturn();
    DepotReadDto depot = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), DepotReadDto.class);
    assertThat(depot.getId()).isEqualTo(2);
    assertThat(depot.getVersion()).isEqualTo(1);
    assertThat(depot.getName()).isEqualTo("My other depot");
    assertThat(depot.getCurrency()).isEqualTo("EUR");
    verifyLogoLink(depot, false);

  }

  @Test
  void getDepot_etf_ok() throws Exception {
    MvcResult mvcResult = getDepot(4L).andExpect(status().isOk()).andReturn();
    DepotReadDto depot = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), DepotReadDto.class);
    assertThat(depot.getId()).isEqualTo(4);
    assertThat(depot.getVersion()).isEqualTo(0);
    assertThat(depot.getName()).isEqualTo("ETF");
    assertThat(depot.getCurrency()).isEqualTo("EUR");
    verifyLogoLink(depot, true);
  }

  @Test
  void getDepot_groupedSecurities_ok() throws Exception {
    MvcResult mvcResult = getDepot(7L).andExpect(status().isOk()).andReturn();
    DepotReadDto depot = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), DepotReadDto.class);
    assertThat(depot.getId()).isEqualTo(7);
    assertThat(depot.getVersion()).isEqualTo(1);
    assertThat(depot.getName()).isEqualTo("Grouped Securities");
    assertThat(depot.getCurrency()).isEqualTo("EUR");
  }

  @Test
  void getDepot_ungroupedSecurities_ok() throws Exception {
    MvcResult mvcResult = getDepot(8L).andExpect(status().isOk()).andReturn();
    DepotReadDto depot = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), DepotReadDto.class);
    assertThat(depot.getId()).isEqualTo(8);
    assertThat(depot.getVersion()).isEqualTo(0);
    assertThat(depot.getName()).isEqualTo("Ungrouped Securities");
    assertThat(depot.getCurrency()).isEqualTo("EUR");
  }

  private ResultActions getDepot(Long id) throws Exception {
    return mockMvc.perform(get(String.format(ENDPOINT, id)));
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
