package de.as.fynancials.depot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.as.fynancials.depot.api.model.DepotCreateDto;
import de.as.fynancials.depot.api.model.DepotReadDto;
import integration.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class CreateDepotTest {

  private static final String ENDPOINT = "/depots";

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private DepotRepository depotRepository;

  private DepotCreateDto requestBody;

  @BeforeEach
  void beforeEach() {
    requestBody = new DepotCreateDto();
    requestBody.setName("My new Depot");
    requestBody.setCurrency("EUR");
  }

  @Test
  void createDepot_ok() throws Exception {
    runPositiveTestCase();
  }

  @Test
  void createDepot_USD_ok() throws Exception {
    requestBody.setCurrency("USD");
    runPositiveTestCase();
  }

  @Test
  void createDepot_trimName_ok() throws Exception {
    requestBody.setName("  this will be trimmed\t");
    DepotReadDto responseBody = runPositiveTestCase();
    assertThat(responseBody.getName()).isEqualTo("this will be trimmed");
    DepotEntity entity = depotRepository.findById(responseBody.getId()).orElseThrow();
    assertThat(entity.getName()).isEqualTo("this will be trimmed");
  }

  @Test
  void createDepot_noName_badRequest() throws Exception {
    requestBody.setName(null);
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createDepot_emptyName_badRequest() throws Exception {
    requestBody.setName("");
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createDepot_blankName_badRequest() throws Exception {
    requestBody.setName("  \t ");
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createDepot_nameExistsAlready_conflict() throws Exception {
    requestBody.setName("My other depot");
    runNegativeTestCase(HttpStatus.CONFLICT);
  }

  @Test
  void createDepot_trimmedNameExistsAlready_conflict() throws Exception {
    requestBody.setName(" My other depot ");
    runNegativeTestCase(HttpStatus.CONFLICT);
  }

  @Test
  void createDepot_unsupportedCurrency_badRequest() throws Exception {
    requestBody.setCurrency("ABC");
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createDepot_noCurrency_badRequest() throws Exception {
    requestBody.setCurrency(null);
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createDepot_emptyCurrency_badRequest() throws Exception {
    requestBody.setCurrency("");
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createDepot_blankCurrency_badRequest() throws Exception {
    requestBody.setCurrency("   ");
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  private DepotReadDto runPositiveTestCase() throws Exception {
    long numberOfDepots = depotRepository.count();
    MvcResult mvcResult = postDepot().andExpect(status().isCreated()).andReturn();

    // verify response
    DepotReadDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), DepotReadDto.class);
    verifyResponseBody(responseBody);
    verifyLocationHeader(mvcResult);

    // verify database
    assertThat(depotRepository.count()).isEqualTo(numberOfDepots + 1);
    verifyDatabase(responseBody.getId());
    return responseBody;
  }

  private void runNegativeTestCase(HttpStatus expectedStatus) throws Exception {
    long numberOfDepots = depotRepository.count();
    MvcResult mvcResult = postDepot().andExpect(status().is(expectedStatus.value())).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(mvcResult.getResponse().getHeader("Location")).isNull();
    assertThat(depotRepository.count()).isEqualTo(numberOfDepots);
  }

  private ResultActions postDepot() throws Exception {
    return mockMvc.perform(
        post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestBody)));
  }

  private void verifyDatabase(long expectedId) {
    DepotEntity entity = depotRepository.findById(expectedId).orElseThrow();
    assertThat(entity.getVersion()).isEqualTo(0);
    assertThat(entity.getName()).isEqualTo(requestBody.getName().trim());
    assertThat(entity.getCurrency()).isEqualTo(requestBody.getCurrency());
  }

  private void verifyResponseBody(DepotReadDto responseBody) {
    assertThat(responseBody.getId()).isGreaterThan(0);
    assertThat(responseBody.getVersion()).isEqualTo(0);
    assertThat(responseBody.getName()).isEqualTo(requestBody.getName().trim());
    assertThat(responseBody.getCurrency()).isEqualTo(requestBody.getCurrency());
    assertThat(responseBody.getLinks()).isNotNull();
    assertThat(responseBody.getLinks().getLogo()).isNull();
  }

  private void verifyLocationHeader(MvcResult mvcResult) throws Exception {
    String locationHeader = mvcResult.getResponse().getHeader("Location");
    DepotReadDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), DepotReadDto.class);
    assertThat(locationHeader).isEqualTo(ENDPOINT + "/" + responseBody.getId());
  }
}
