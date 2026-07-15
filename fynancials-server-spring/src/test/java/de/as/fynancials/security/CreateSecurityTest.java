package de.as.fynancials.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.as.fynancials.security.api.model.SecurityCreateDto;
import de.as.fynancials.security.api.model.SecurityReadDto;
import de.as.fynancials.security.api.model.SecurityTypeDto;
import integration.IntegrationTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class CreateSecurityTest {

  private static final String ENDPOINT = "/securities";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private SecurityRepository securityRepository;

  private ObjectMapper objectMapper;

  private SecurityCreateDto requestBody;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper().findAndRegisterModules();
    requestBody = new SecurityCreateDto();
    requestBody.setIsin("US09247X1019");
    requestBody.setSymbols(List.of("BLK", "BLQA"));
    requestBody.setName("Blackrock");
    requestBody.setWkn("928193");
    requestBody.setSector("Financial");
    requestBody.setSecurityType(SecurityTypeDto.STOCK);
  }

  @Test
  void createStock_allProperties_success() throws Exception {
    runPositiveTestCase();
  }

  @Test
  void createStock_noWkn_success() throws Exception {
    requestBody.setWkn(null);
    runPositiveTestCase();
  }

  @Test
  void createStock_lettersInWkn_success() throws Exception {
    requestBody.setWkn("ABCDEF");
    runPositiveTestCase();
  }

  @Test
  void createStock_noSector_success() throws Exception {
    requestBody.setSector(null);
    runPositiveTestCase();
  }

  @Test
  void createStock_symbolsIsEmpty_success() throws Exception {
    requestBody.setSymbols(List.of());
    runPositiveTestCase();
  }

  @Test
  void createStock_nameIsTrimmed_success() throws Exception {
    requestBody.setName(String.format("  %s  ", requestBody.getName()));
    long numberOfSecurities = securityRepository.count();
    MvcResult result = postSecurity().andExpect(status().isCreated()).andReturn();

    // verify response
    SecurityReadDto responseBody =
        objectMapper.readValue(result.getResponse().getContentAsString(), SecurityReadDto.class);
    requestBody.setName(responseBody.getName().trim());
    verifyResponseBody(responseBody);
    verifyLocationHeader(result);

    // verify database
    assertThat(securityRepository.count()).isEqualTo(numberOfSecurities + 1);
    verifyDatabase(responseBody.getId());
  }

  @Test
  void createStock_sectorIsTrimmed_success() throws Exception {
    requestBody.setSector(String.format("  %s  ", requestBody.getSector()));
    long numberOfSecurities = securityRepository.count();
    MvcResult result = postSecurity().andExpect(status().isCreated()).andReturn();

    // verify response
    SecurityReadDto responseBody =
        objectMapper.readValue(result.getResponse().getContentAsString(), SecurityReadDto.class);
    requestBody.setSector(responseBody.getSector().trim());
    verifyResponseBody(responseBody);
    verifyLocationHeader(result);

    // verify database
    assertThat(securityRepository.count()).isEqualTo(numberOfSecurities + 1);
    verifyDatabase(responseBody.getId());
  }

  @Test
  void createStock_SymbolsAreTrimmed_success() throws Exception {
    requestBody.setSymbols(List.of("  BLK  ", "  BLQA  "));
    long numberOfSecurities = securityRepository.count();
    MvcResult result = postSecurity().andExpect(status().isCreated()).andReturn();

    // verify response
    SecurityReadDto responseBody =
        objectMapper.readValue(result.getResponse().getContentAsString(), SecurityReadDto.class);
    requestBody.setSymbols(List.of("BLK", "BLQA"));
    verifyResponseBody(responseBody);
    verifyLocationHeader(result);

    // verify database
    assertThat(securityRepository.count()).isEqualTo(numberOfSecurities + 1);
    verifyDatabase(responseBody.getId());
  }

  @Test
  void createEtf_success() throws Exception {
    requestBody = new SecurityCreateDto();
    requestBody.setIsin("IE00B5BMR087");
    requestBody.setSymbols(List.of("SXR8"));
    requestBody.setName("iShares Core S&P 500 UCITS ETF (Acc)");
    requestBody.setWkn("A0YEDG");
    requestBody.setSector(null);
    requestBody.setSecurityType(SecurityTypeDto.ETF);
    runPositiveTestCase();
  }

  @Test
  void createOther_success() throws Exception {
    requestBody.setSecurityType(SecurityTypeDto.OTHER);
    runPositiveTestCase();
  }

  @Test
  void createStock_duplicateIsin_fail() throws Exception {
    requestBody.setIsin("NL0010273215");
    runNegativeTestCase(HttpStatus.CONFLICT);
  }

  @Test
  void createStock_duplicateWkn_fail() throws Exception {
    requestBody.setWkn("870747");
    runNegativeTestCase(HttpStatus.CONFLICT);
  }

  @Test
  void createStock_noIsin_fail() throws Exception {
    requestBody.setIsin(null);
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createStock_shortIsin_fail() throws Exception {
    requestBody.setIsin("US09247X101");
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createStock_longIsin_fail() throws Exception {
    requestBody.setIsin("US09247X1019A");
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createStock_wrongFormattedIsin_fail() throws Exception {
    requestBody.setIsin("1109247X1019");
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createStock_lowerCaseIsin_fail() throws Exception {
    requestBody.setIsin("us09247x1019");
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createStock_symbolsIsNull_fail() throws Exception {
    requestBody.setSymbols(null);
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createStock_duplicateSymbolInDb_fail() throws Exception {
    requestBody.setSymbols(List.of("BLK", "BLQA", "MSFT"));
    runNegativeTestCase(HttpStatus.CONFLICT);
  }

  @Test
  void createStock_nameIsEmpty_fail() throws Exception {
    requestBody.setName("");
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createStock_nameIsBlank_fail() throws Exception {
    requestBody.setName(" ");
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createStock_wknContainsBlanks_fail() throws Exception {
    requestBody.setWkn("92819 ");
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createStock_wknIsEmpty_fail() throws Exception {
    requestBody.setWkn("");
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createStock_wknIsBlank_fail() throws Exception {
    requestBody.setWkn("      ");
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createStock_shortWkn_fail() throws Exception {
    requestBody.setWkn("92819");
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createStock_longWkn_fail() throws Exception {
    requestBody.setWkn("0928193");
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createStock_lowerCaseWkn_fail() throws Exception {
    requestBody.setWkn("abcdef");
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createStock_sectorIsEmpty_fail() throws Exception {
    requestBody.setSector("");
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createStock_sectorIsBlank_fail() throws Exception {
    requestBody.setSector(" ");
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void noRequestBody_badRequest() throws Exception {
    long count = securityRepository.count();
    MvcResult result =
        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
            .andReturn();
    assertThat(result.getResponse().getContentLength()).isZero();
    assertThat(securityRepository.count()).isEqualTo(count);
  }

  private void runPositiveTestCase() throws Exception {
    long numberOfSecurities = securityRepository.count();
    MvcResult result = postSecurity().andExpect(status().isCreated()).andReturn();

    // verify response
    SecurityReadDto responseBody =
        objectMapper.readValue(result.getResponse().getContentAsString(), SecurityReadDto.class);
    verifyResponseBody(responseBody);
    verifyLocationHeader(result);

    // verify database
    assertThat(securityRepository.count()).isEqualTo(numberOfSecurities + 1);
    verifyDatabase(responseBody.getId());
  }

  private void runNegativeTestCase(HttpStatus expectedStatus) throws Exception {
    long numberOfSecurities = securityRepository.count();
    MvcResult mvcResult = postSecurity().andExpect(status().is(expectedStatus.value())).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(securityRepository.count()).isEqualTo(numberOfSecurities);
  }

  private ResultActions postSecurity() throws Exception {
    return mockMvc.perform(
        post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestBody)));
  }

  private void verifyDatabase(long expectedId) {
    SecurityEntity entity = securityRepository.findById(expectedId).orElseThrow();
    assertThat(entity.getId()).isEqualTo(expectedId);
    assertThat(entity.getVersion()).isEqualTo(0);
    assertThat(entity.getIsin()).isEqualTo(requestBody.getIsin());
    assertThat(entity.getSymbols()).hasSameElementsAs(requestBody.getSymbols());
    assertThat(entity.getWkn()).isEqualTo(requestBody.getWkn());
    assertThat(entity.getSector()).isEqualTo(requestBody.getSector());
    assertThat(entity.getSecurityType()).isEqualTo(requestBody.getSecurityType());
  }

  private void verifyResponseBody(SecurityReadDto responseBody) {
    assertThat(responseBody.getId()).isGreaterThan(0);
    assertThat(responseBody.getVersion()).isEqualTo(0);
    assertThat(responseBody.getIsin()).isEqualTo(requestBody.getIsin());
    assertThat(responseBody.getSymbols()).hasSameElementsAs(requestBody.getSymbols());
    assertThat(responseBody.getWkn()).isEqualTo(requestBody.getWkn());
    assertThat(responseBody.getSector()).isEqualTo(requestBody.getSector());
    assertThat(responseBody.getSecurityType()).isEqualTo(requestBody.getSecurityType());
  }

  private void verifyLocationHeader(MvcResult result) throws Exception {
    String locationHeader = result.getResponse().getHeader("Location");
    SecurityReadDto responseBody =
        objectMapper.readValue(result.getResponse().getContentAsString(), SecurityReadDto.class);
    assertThat(locationHeader).isEqualTo(ENDPOINT + "/" + responseBody.getId());
  }
}
