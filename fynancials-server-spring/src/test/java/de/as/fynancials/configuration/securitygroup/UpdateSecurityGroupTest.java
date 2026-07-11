package de.as.fynancials.configuration.securitygroup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.as.fynancials.config.securitygroup.api.model.SecurityGroupReadDto;
import de.as.fynancials.config.securitygroup.api.model.SecurityGroupUpdateDto;
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
class UpdateSecurityGroupTest {

  private static final String ENDPOINT = "/config/security-groups/%d";

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private SecurityGroupRepository securityGroupRepository;

  private SecurityGroupUpdateDto requestBody;

  @BeforeEach
  void beforeEach() {
    requestBody = new SecurityGroupUpdateDto();
    requestBody.setName("Volkswagen");
    requestBody.setSecurities(
        List.of(SecurityIds.VW_VZ, SecurityIds.VW_STAMM, SecurityIds.VW_VZ_ADR, SecurityIds.VW_STAMM_ADR));
    requestBody.setVersion(1L);
  }

  @Test
  void updateSecurityGroup_updateNothing_ok() throws Exception {
    runPositiveTestCase(1);
  }

  @Test
  void updateSecurityGroup_updateName_ok() throws Exception {
    requestBody.setName("Volkswagen AG");
    runPositiveTestCase(1);
  }

  @Test
  void updateSecurityGroup_trimName_ok() throws Exception {
    requestBody.setName("  \t VW  ");
    runPositiveTestCase(1);
  }

  @Test
  void updateSecurityGroup_removeSecurities_ok() throws Exception {
    requestBody.setSecurities(List.of(SecurityIds.VW_VZ, SecurityIds.VW_STAMM));
    runPositiveTestCase(1);
  }

  @Test
  void updateSecurityGroup_addSecurities_ok() throws Exception {
    requestBody.setSecurities(
        List.of(SecurityIds.VW_VZ, SecurityIds.VW_STAMM, SecurityIds.VW_VZ_ADR, SecurityIds.VW_STAMM_ADR,
            SecurityIds.MSFT, SecurityIds.AAPL));
    runPositiveTestCase(1);
  }

  @Test
  void updateSecurityGroup_replaceSecuritiesEntirely_ok() throws Exception {
    requestBody.setSecurities(List.of(SecurityIds.MSFT, SecurityIds.AAPL, SecurityIds.HAG));
    runPositiveTestCase(1);
  }

  @Test
  void updateSecurityGroup_updateEverything_ok() throws Exception {
    requestBody.setName("other name");
    requestBody.setSecurities(List.of(SecurityIds.MSFT, SecurityIds.AAPL));
    runPositiveTestCase(1);
  }

  @Test
  void updateSecurityGroup_containsDuplicateSecurityId_ok() throws Exception {
    requestBody.setSecurities(List.of(SecurityIds.MSFT, SecurityIds.AAPL, SecurityIds.MSFT));
    runPositiveTestCase(1);
  }

  @Test
  void updateSecurityGroup_optimisticLockFails_conflict() throws Exception {
    requestBody.setVersion(0L);
    runNegativeTestCase(1, HttpStatus.CONFLICT);
  }

  @Test
  void updateSecurityGroup_nameExists_conflict() throws Exception {
    requestBody.setName("Alphabet");
    runNegativeTestCase(1, HttpStatus.CONFLICT);
  }

  @Test
  void updateSecurityGroup_trimmedNameExists_conflict() throws Exception {
    requestBody.setName("  \t Alphabet  ");
    runNegativeTestCase(1, HttpStatus.CONFLICT);
  }

  @Test
  void updateSecurityGroup_groupDoesNotExist_notFound() throws Exception {
    long securityGroupCount = securityGroupRepository.count();
    MvcResult mvcResult = putSecurityGroup(999).andExpect(status().isNotFound()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(securityGroupRepository.count()).isEqualTo(securityGroupCount);
  }

  @Test
  void updateSecurityGroup_securityIsAlreadyPartOfAnotherGroup() throws Exception {
    requestBody.setSecurities(List.of(SecurityIds.GOOGL, SecurityIds.HAG));
    runNegativeTestCase(1, HttpStatus.BAD_REQUEST);
  }

  @Test
  void updateSecurityGroup_securityDoesNotExist_badRequest() throws Exception {
    requestBody.setSecurities(List.of(SecurityIds.MSFT, 999L));
    runNegativeTestCase(1, HttpStatus.BAD_REQUEST);
  }

  @Test
  void updateSecurityGroup_sizeOne_badRequest() throws Exception {
    requestBody.setSecurities(List.of(SecurityIds.MSFT));
    runNegativeTestCase(1, HttpStatus.BAD_REQUEST);
  }

  @Test
  void updateSecurityGroup_onlyOneDuplicateSecurity_badRequest() throws Exception {
    requestBody.setSecurities(List.of(SecurityIds.MSFT, SecurityIds.MSFT));
    runNegativeTestCase(1, HttpStatus.BAD_REQUEST);
  }

  @Test
  void updateSecurityGroup_empty_badRequest() throws Exception {
    requestBody.setSecurities(List.of());
    runNegativeTestCase(1, HttpStatus.BAD_REQUEST);
  }

  @Test
  void updateSecurityGroup_securitiesIsNull_badRequest() throws Exception {
    requestBody.setSecurities(null);
    runNegativeTestCase(1, HttpStatus.BAD_REQUEST);
  }

  private ResultActions putSecurityGroup(long groupId) throws Exception {
    return mockMvc.perform(put(String.format(ENDPOINT, groupId)).contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestBody)));
  }

  private void runPositiveTestCase(long groupId) throws Exception {
    long securityGroupCount = securityGroupRepository.count();
    MvcResult mvcResult = putSecurityGroup(groupId).andExpect(status().isOk()).andReturn();

    SecurityGroupReadDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), SecurityGroupReadDto.class);
    verifyResponseBody(responseBody, groupId);
    verifyDatabase(groupId);
    assertThat(securityGroupRepository.count()).isEqualTo(securityGroupCount);
  }

  private void runNegativeTestCase(long groupId, HttpStatus expectedStatus) throws Exception {
    SecurityGroupEntity entity = securityGroupRepository.findById(groupId).orElseThrow();
    long version = entity.getVersion();
    String name = entity.getName();
    Set<Long> securities = entity.getSecurities();
    long securityGroupCount = securityGroupRepository.count();

    MvcResult mvcResult = putSecurityGroup(groupId).andExpect(status().is(expectedStatus.value())).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(securityGroupRepository.count()).isEqualTo(securityGroupCount);

    entity = securityGroupRepository.findById(groupId).orElseThrow();
    assertThat(entity.getVersion()).isEqualTo(version);
    assertThat(entity.getName()).isEqualTo(name);
    assertThat(entity.getSecurities()).hasSameSizeAs(securities);
    assertThat(entity.getSecurities()).containsAll(securities);
  }

  private void verifyDatabase(long expectedGroupId) {
    SecurityGroupEntity entity = securityGroupRepository.findById(expectedGroupId).orElseThrow();
    assertThat(entity.getName()).isEqualTo(requestBody.getName().trim());
    assertThat(entity.getVersion()).isEqualTo(requestBody.getVersion() + 1);

    Set<Long> expectedSecurities = new HashSet<>(requestBody.getSecurities());
    assertThat(entity.getSecurities()).hasSameSizeAs(expectedSecurities);
    assertThat(entity.getSecurities()).containsAll(expectedSecurities);
  }

  private void verifyResponseBody(SecurityGroupReadDto responseBody, long expectedGroupId) {
    assertThat(responseBody.getId()).isEqualTo(expectedGroupId);
    assertThat(responseBody.getName()).isEqualTo(requestBody.getName().trim());
    assertThat(responseBody.getVersion()).isEqualTo(requestBody.getVersion() + 1);

    Set<Long> expectedSecurities = new HashSet<>(requestBody.getSecurities());
    assertThat(responseBody.getSecurities()).hasSameSizeAs(expectedSecurities);
    assertThat(responseBody.getSecurities()).containsAll(expectedSecurities);
  }
}
