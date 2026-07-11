package de.as.fynancials.configuration.securitygroup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.as.fynancials.config.securitygroup.api.model.SecurityGroupCreateDto;
import de.as.fynancials.config.securitygroup.api.model.SecurityGroupReadDto;
import integration.IntegrationTest;
import integration.SecurityIds;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class CreateSecurityGroupTest {

  private static final String ENDPOINT = "/config/security-groups";

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private SecurityGroupRepository securityGroupRepository;

  private SecurityGroupCreateDto requestBody;

  @BeforeEach
  void beforeEach() {
    requestBody = new SecurityGroupCreateDto();
    requestBody.setName("New Security Group");
    requestBody.setSecurities(List.of(11L, 12L));
  }

  @Test
  void createGroup_minSize_ok() throws Exception {
    runPositiveTestCase();
  }

  @Test
  void createGroup_moreStocks_ok() throws Exception {
    requestBody.setName("My favorite stocks");
    requestBody.setSecurities(List.of(1L, 3L, 5L, 8L, 16L, 28L));
    runPositiveTestCase();
  }

  @Test
  void createGroup_containsDuplicateSecurityId_ok() throws Exception {
    requestBody.setSecurities(List.of(11L, 12L, 12L));
    runPositiveTestCase();
  }

  @Test
  void createGroup_nameIsEmpty_badRequest() throws Exception {
    requestBody.setName("");
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createGroup_nameIsBlank_badRequest() throws Exception {
    requestBody.setName(" \t   ");
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createGroup_duplicateName_conflict() throws Exception {
    requestBody.setName("Volkswagen");
    runNegativeTestCase(HttpStatus.CONFLICT);
  }

  @Test
  void createGroup_duplicateTrimmedName_conflict() throws Exception {
    requestBody.setName("  \t Volkswagen    ");
    runNegativeTestCase(HttpStatus.CONFLICT);
  }

  @Test
  void createGroup_securityIsAlreadyPartOfAnotherGroup_badRequest() throws Exception {
    requestBody.setSecurities(List.of(9L, 10L, 37L));
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createGroup_securityDoesNotExist_badRequest() throws Exception {
    requestBody.setSecurities(List.of(777L, 888L));
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createGroup_sizeOne_badRequest() throws Exception {
    requestBody.setSecurities(List.of(1L));
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createGroup_onlyOneDuplicateSecurity_badRequest() throws Exception {
    requestBody.setSecurities(List.of(SecurityIds.MSFT, SecurityIds.MSFT));
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createGroup_empty_badRequest() throws Exception {
    requestBody.setSecurities(List.of());
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  @Test
  void createGroup_securitiesIsNull_badRequest() throws Exception {
    requestBody.setSecurities(null);
    runNegativeTestCase(HttpStatus.BAD_REQUEST);
  }

  private void runPositiveTestCase() throws Exception {
    long numberOfGroups = securityGroupRepository.count();
    MvcResult mvcResult = postSecurityGroup().andExpect(status().isCreated()).andReturn();

    // verify response
    SecurityGroupReadDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), SecurityGroupReadDto.class);
    verifyResponseBody(responseBody);
    verifyLocationHeader(mvcResult);

    // verify database
    assertThat(securityGroupRepository.count()).isEqualTo(numberOfGroups + 1);
    verifyDatabase(responseBody.getId());
  }

  private void runNegativeTestCase(HttpStatus expectedStatus) throws Exception {
    long numberOfGroups = securityGroupRepository.count();
    MvcResult mvcResult = postSecurityGroup().andExpect(status().is(expectedStatus.value())).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(mvcResult.getResponse().getHeader("Location")).isNull();
    assertThat(securityGroupRepository.count()).isEqualTo(numberOfGroups);
  }

  private ResultActions postSecurityGroup() throws Exception {
    return mockMvc.perform(
        post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(requestBody)));
  }

  private void verifyDatabase(long expectedGroupId) {
    SecurityGroupEntity entity = securityGroupRepository.findById(expectedGroupId).orElseThrow();
    assertThat(entity.getName()).isEqualTo(requestBody.getName().trim());
    assertThat(entity.getVersion()).isEqualTo(0);

    Set<Long> expectedSecurities = new HashSet<>(requestBody.getSecurities());
    assertThat(entity.getSecurities()).hasSameSizeAs(expectedSecurities);
    assertThat(entity.getSecurities()).containsAll(expectedSecurities);
  }

  private void verifyResponseBody(SecurityGroupReadDto responseBody) {
    assertThat(responseBody.getId()).isGreaterThan(0);
    assertThat(responseBody.getVersion()).isEqualTo(0);
    assertThat(responseBody.getName()).isEqualTo(requestBody.getName().trim());

    Set<Long> expectedSecurities = new HashSet<>(requestBody.getSecurities());
    assertThat(responseBody.getSecurities()).hasSameSizeAs(expectedSecurities);
    assertThat(responseBody.getSecurities()).containsAll(expectedSecurities);
  }

  private void verifyLocationHeader(MvcResult mvcResult) throws Exception {
    String locationHeader = mvcResult.getResponse().getHeader("Location");
    SecurityGroupReadDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), SecurityGroupReadDto.class);
    assertThat(locationHeader).isEqualTo(ENDPOINT + "/" + responseBody.getId());
  }
}
