package de.as.fynancials.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.as.fynancials.security.api.model.SecurityReadDto;
import de.as.fynancials.security.api.model.SecurityTypeDto;
import de.as.fynancials.security.api.model.SecurityUpdateDto;
import integration.IntegrationTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class UpdateSecurityTest {

  private static final String ENDPOINT = "/securities/%d";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private SecurityRepository securityRepository;

  private ObjectMapper objectMapper;

  private SecurityUpdateDto requestBody;

  private long securityCount;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper().findAndRegisterModules();
    securityCount = securityRepository.count();
    requestBody = new SecurityUpdateDto();
    requestBody.setIsin("US5949181045");
    requestBody.setSymbols(List.of("MSFT", "MSF.DE"));
    requestBody.setName("Microsoft");
    requestBody.setWkn("870747");
    requestBody.setSector("Technology");
    requestBody.setSecurityType(SecurityTypeDto.STOCK);
    requestBody.setVersion(1L);
  }

  @Test
  void updateAllProperties_success() throws Exception {
    requestBody = new SecurityUpdateDto();
    requestBody.setIsin("US09247X1019");
    requestBody.setSymbols(List.of("BLK", "BLQA"));
    requestBody.setName("Blackrock");
    requestBody.setWkn("928193");
    requestBody.setSector("Financial");
    requestBody.setSecurityType(SecurityTypeDto.STOCK);
    requestBody.setVersion(1L);

    MvcResult result = putSecurity(1).andExpect(status().isOk()).andReturn();

    // verify response
    SecurityReadDto responseBody =
        objectMapper.readValue(result.getResponse().getContentAsString(), SecurityReadDto.class);
    verifyResponseBody(responseBody);

    // verify database
    assertThat(securityRepository.count()).isEqualTo(securityCount);
    verifyDatabase(1);
  }

  @Test
  void updateNothing_success() throws Exception {
    MvcResult result = putSecurity(1).andExpect(status().isOk()).andReturn();

    // verify response
    SecurityReadDto responseBody =
        objectMapper.readValue(result.getResponse().getContentAsString(), SecurityReadDto.class);
    verifyResponseBody(responseBody);

    // verify database
    assertThat(securityRepository.count()).isEqualTo(securityCount);
    verifyDatabase(1);
  }

  @Test
  void updateType_toEtf_ok() throws Exception {
    requestBody.setSecurityType(SecurityTypeDto.ETF);

    MvcResult result = putSecurity(1).andExpect(status().isOk()).andReturn();

    // verify response
    SecurityReadDto responseBody =
        objectMapper.readValue(result.getResponse().getContentAsString(), SecurityReadDto.class);
    verifyResponseBody(responseBody);

    // verify database
    assertThat(securityRepository.count()).isEqualTo(securityCount);
    verifyDatabase(1);
  }

  @Test
  void updateType_toOther_ok() throws Exception {
    requestBody.setSecurityType(SecurityTypeDto.OTHER);

    MvcResult result = putSecurity(1).andExpect(status().isOk()).andReturn();

    // verify response
    SecurityReadDto responseBody =
        objectMapper.readValue(result.getResponse().getContentAsString(), SecurityReadDto.class);
    verifyResponseBody(responseBody);

    // verify database
    assertThat(securityRepository.count()).isEqualTo(securityCount);
    verifyDatabase(1);
  }

  @Test
  void update_notFound() throws Exception {
    MvcResult result = putSecurity(999).andExpect(status().isNotFound()).andReturn();

    // verify response
    assertThat(result.getResponse().getContentLength()).isZero();

    // verify database
    assertThat(securityRepository.count()).isEqualTo(securityCount);
  }

  @Test
  void updateAllProperties_optimisticLockFails_conflict() throws Exception {
    requestBody.setVersion(0L);
    SecurityEntity msftBeforeRequest = securityRepository.findById(1L).orElseThrow();
    MvcResult result = putSecurity(1).andExpect(status().isConflict()).andReturn();

    // verify response
    assertThat(result.getResponse().getContentLength()).isZero();

    // verify database
    SecurityEntity msftAfterRequest = securityRepository.findById(1L).orElseThrow();
    assertSecurityUnchanged(msftBeforeRequest, msftAfterRequest);
    assertThat(securityRepository.count()).isEqualTo(securityCount);
  }

  @Test
  void updateIsin_conflict() throws Exception {
    requestBody.setIsin("US0378331005");
    SecurityEntity msftBeforeRequest = securityRepository.findById(1L).orElseThrow();
    MvcResult result = putSecurity(1).andExpect(status().isConflict()).andReturn();

    // verify response
    assertThat(result.getResponse().getContentLength()).isZero();

    // verify database
    SecurityEntity msftAfterRequest = securityRepository.findById(1L).orElseThrow();
    assertSecurityUnchanged(msftBeforeRequest, msftAfterRequest);
    assertThat(securityRepository.count()).isEqualTo(securityCount);
  }

  @Test
  void updateWkn_conflict() throws Exception {
    requestBody.setWkn("HAG000");
    SecurityEntity msftBeforeRequest = securityRepository.findById(1L).orElseThrow();
    MvcResult result = putSecurity(1).andExpect(status().isConflict()).andReturn();

    // verify response
    assertThat(result.getResponse().getContentLength()).isZero();

    // verify database
    SecurityEntity msftAfterRequest = securityRepository.findById(1L).orElseThrow();
    assertSecurityUnchanged(msftBeforeRequest, msftAfterRequest);
    assertThat(securityRepository.count()).isEqualTo(securityCount);
  }

  @Test
  void updateSymbols_conflict() throws Exception {
    requestBody.setSymbols(List.of("MSFT", "MSF.DE", "AAPL"));
    SecurityEntity msftBeforeRequest = securityRepository.findById(1L).orElseThrow();
    MvcResult result = putSecurity(1).andExpect(status().isConflict()).andReturn();

    // verify response
    assertThat(result.getResponse().getContentLength()).isZero();

    // verify database
    SecurityEntity msftAfterRequest = securityRepository.findById(1L).orElseThrow();
    assertSecurityUnchanged(msftBeforeRequest, msftAfterRequest);
    assertThat(securityRepository.count()).isEqualTo(securityCount);

  }

  @Test
  void noRequestBody_badRequest() throws Exception {
    long count = securityRepository.count();
    SecurityEntity msftBeforeRequest = securityRepository.findById(1L).orElseThrow();
    MvcResult result = mockMvc.perform(put(String.format(ENDPOINT, 2)).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest()).andReturn();
    assertThat(result.getResponse().getContentLength()).isZero();
    assertThat(securityRepository.count()).isEqualTo(count);
    SecurityEntity msftAfterRequest = securityRepository.findById(1L).orElseThrow();
    assertSecurityUnchanged(msftBeforeRequest, msftAfterRequest);
  }

  @Test
  void removeSymbols_ok() throws Exception {
    requestBody.setSymbols(List.of());
    MvcResult result = putSecurity(1).andExpect(status().isOk()).andReturn();

    // verify response
    SecurityReadDto responseBody =
        objectMapper.readValue(result.getResponse().getContentAsString(), SecurityReadDto.class);
    verifyResponseBody(responseBody);

    // verify database
    assertThat(securityRepository.count()).isEqualTo(securityCount);
    verifyDatabase(1);
  }

  @Test
  void setSymbolsToNull_badRequest() throws Exception {
    requestBody.setSymbols(null);
    SecurityEntity msftBeforeRequest = securityRepository.findById(1L).orElseThrow();
    MvcResult result = putSecurity(1).andExpect(status().isBadRequest()).andReturn();

    // verify response
    assertThat(result.getResponse().getContentLength()).isZero();

    // verify database
    SecurityEntity msftAfterRequest = securityRepository.findById(1L).orElseThrow();
    assertSecurityUnchanged(msftBeforeRequest, msftAfterRequest);
    assertThat(securityRepository.count()).isEqualTo(securityCount);
  }

  @Test
  void removeWkn_ok() throws Exception {
    requestBody.setWkn(null);
    MvcResult result = putSecurity(1).andExpect(status().isOk()).andReturn();

    // verify response
    SecurityReadDto responseBody =
        objectMapper.readValue(result.getResponse().getContentAsString(), SecurityReadDto.class);
    verifyResponseBody(responseBody);

    // verify database
    assertThat(securityRepository.count()).isEqualTo(securityCount);
    verifyDatabase(1);
  }

  @Test
  void setWknToEmptyString_badRequest() throws Exception {
    requestBody.setWkn("");
    SecurityEntity msftBeforeRequest = securityRepository.findById(1L).orElseThrow();
    MvcResult result = putSecurity(1).andExpect(status().isBadRequest()).andReturn();

    // verify response
    assertThat(result.getResponse().getContentLength()).isZero();

    // verify database
    SecurityEntity msftAfterRequest = securityRepository.findById(1L).orElseThrow();
    assertSecurityUnchanged(msftBeforeRequest, msftAfterRequest);
    assertThat(securityRepository.count()).isEqualTo(securityCount);
  }

  @Test
  void removeSector_ok() throws Exception {
    requestBody.setSector(null);
    MvcResult result = putSecurity(1).andExpect(status().isOk()).andReturn();

    // verify response
    SecurityReadDto responseBody =
        objectMapper.readValue(result.getResponse().getContentAsString(), SecurityReadDto.class);
    verifyResponseBody(responseBody);

    // verify database
    assertThat(securityRepository.count()).isEqualTo(securityCount);
    verifyDatabase(1);
  }

  @Test
  void setSectorToEmptyString_badRequest() throws Exception {
    requestBody.setSector("");
    SecurityEntity msftBeforeRequest = securityRepository.findById(1L).orElseThrow();
    MvcResult result = putSecurity(1).andExpect(status().isBadRequest()).andReturn();

    // verify response
    assertThat(result.getResponse().getContentLength()).isZero();

    // verify database
    SecurityEntity msftAfterRequest = securityRepository.findById(1L).orElseThrow();
    assertSecurityUnchanged(msftBeforeRequest, msftAfterRequest);
    assertThat(securityRepository.count()).isEqualTo(securityCount);
  }

  @Test
  void removeSecurityType_badRequest() throws Exception {
    requestBody.setSecurityType(null);
    SecurityEntity msftBeforeRequest = securityRepository.findById(1L).orElseThrow();
    MvcResult result = putSecurity(1).andExpect(status().isBadRequest()).andReturn();

    // verify response
    assertThat(result.getResponse().getContentLength()).isZero();

    // verify database
    SecurityEntity msftAfterRequest = securityRepository.findById(1L).orElseThrow();
    assertSecurityUnchanged(msftBeforeRequest, msftAfterRequest);
    assertThat(securityRepository.count()).isEqualTo(securityCount);
  }

  private ResultActions putSecurity(long id) throws Exception {
    return mockMvc.perform(put(String.format(ENDPOINT, id)).contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestBody)));
  }

  private void assertSecurityUnchanged(SecurityEntity before, SecurityEntity after) {
    assertThat(after).usingRecursiveComparison().isEqualTo(before);
  }

  private void verifyDatabase(long id) {
    SecurityEntity entity = securityRepository.findById(id).orElseThrow();
    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getVersion()).isEqualTo(2);
    assertThat(entity.getIsin()).isEqualTo(requestBody.getIsin());
    assertThat(entity.getSymbols()).hasSameElementsAs(requestBody.getSymbols());
    assertThat(entity.getWkn()).isEqualTo(requestBody.getWkn());
    assertThat(entity.getSector()).isEqualTo(requestBody.getSector());
    assertThat(entity.getSecurityType()).isEqualTo(requestBody.getSecurityType());
  }

  private void verifyResponseBody(SecurityReadDto responseBody) {
    assertThat(responseBody.getId()).isGreaterThan(0);
    assertThat(responseBody.getVersion()).isEqualTo(2);
    assertThat(responseBody.getIsin()).isEqualTo(requestBody.getIsin());
    assertThat(responseBody.getSymbols()).hasSameElementsAs(requestBody.getSymbols());
    assertThat(responseBody.getWkn()).isEqualTo(requestBody.getWkn());
    assertThat(responseBody.getSector()).isEqualTo(requestBody.getSector());
    assertThat(responseBody.getSecurityType()).isEqualTo(requestBody.getSecurityType());
  }
}
