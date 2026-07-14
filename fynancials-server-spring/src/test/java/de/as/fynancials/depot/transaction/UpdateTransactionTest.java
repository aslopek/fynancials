package de.as.fynancials.depot.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.as.fynancials.depot.transaction.api.model.TransactionReadDto;
import de.as.fynancials.depot.transaction.api.model.TransactionTypeDto;
import de.as.fynancials.depot.transaction.api.model.TransactionUpdateDto;
import integration.IntegrationTest;
import integration.SecurityIds;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class UpdateTransactionTest {

  private static final String ENDPOINT = "/depots/%d/transactions/%d";

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private TransactionRepository transactionRepository;

  private TransactionUpdateDto requestBody;

  @BeforeEach
  void beforeEach() {
    requestBody = new TransactionUpdateDto();
    requestBody.setDate(LocalDate.of(2020, Month.JULY, 13));
    requestBody.setTime("21:09:34");
    requestBody.setSecurityId(SecurityIds.AMZN);
    requestBody.setTransactionType(TransactionTypeDto.BUY);
    requestBody.setSecurityCountOriginal(1.0);
    requestBody.setSecurityCountSplitAdjusted(20.0);
    requestBody.setGrossValue(2807.0);
    requestBody.setTax(null);
    requestBody.setFee(10.0);
    requestBody.setVersion(1L);
  }

  @Test
  void updateTransaction_updateNothing_ok() throws Exception {
    runPositiveTestCase(1, 1);
  }

  @Test
  void updateTransaction_updateEverything_ok() throws Exception {
    runPositiveTestCase(1, 3);
  }

  @Test
  void updateTransaction_wrongVersion_conflict() throws Exception {
    requestBody.setVersion(0L);
    runNegativeTestCase(1, 1, HttpStatus.CONFLICT);
  }

  @Test
  void updateTransaction_transactionDoesNotExist_notFound() throws Exception {
    long transactionCount = transactionRepository.count();
    MvcResult mvcResult = putTransaction(1, 999).andExpect(status().isNotFound()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(transactionRepository.count()).isEqualTo(transactionCount);
  }

  @Test
  void updateTransaction_depotDoesNotExist_notFound() throws Exception {
    runNegativeTestCase(999, 1, HttpStatus.NOT_FOUND);
  }

  @Test
  void updateTransaction_transactionIsAssignedToOtherDepot_notFound() throws Exception {
    runNegativeTestCase(2, 1, HttpStatus.NOT_FOUND);
  }

  @Test
  void updateTransaction_updateDate_ok() throws Exception {
    requestBody.setDate(LocalDate.of(2021, Month.SEPTEMBER, 7));
    runPositiveTestCase(1, 1);
  }

  @Test
  void updateTransaction_removeDate_badRequest() throws Exception {
    requestBody.setDate(null);
    runNegativeTestCase(1, 1, HttpStatus.BAD_REQUEST);
  }

  @Test
  void updateTransaction_updateTime_ok() throws Exception {
    requestBody.setTime("17:54:01");
    runPositiveTestCase(1, 1);
  }

  @Test
  void updateTransaction_removeTime_ok() throws Exception {
    requestBody.setTime(null);
    runPositiveTestCase(1, 1);
  }

  @Test
  void updateTransaction_wrongTime_badRequest() throws Exception {
    requestBody.setTime("25:00:00");
    runNegativeTestCase(1, 1, HttpStatus.BAD_REQUEST);
  }

  @Test
  void updateTransaction_updateSecurityId_ok() throws Exception {
    requestBody.setSecurityId(SecurityIds.HAG);
    runPositiveTestCase(1, 1);
  }

  @Test
  void updateTransaction_updateSecurityId_securityIdDoesNotExist_badRequest() throws Exception {
    requestBody.setSecurityId(999L);
    runNegativeTestCase(1, 1, HttpStatus.BAD_REQUEST);
  }

  @Test
  void updateTransaction_updateTransactionType_ok() throws Exception {
    requestBody.setTransactionType(TransactionTypeDto.DIVIDEND);
    runPositiveTestCase(1, 1);
  }

  @Test
  void updateTransaction_updateSecurityCountOriginal_ok() throws Exception {
    requestBody.setSecurityCountOriginal(2.0);
    runPositiveTestCase(1, 1);
  }

  @Test
  void updateTransaction_updateSecurityCountOriginal_fractionalShares_ok() throws Exception {
    requestBody.setSecurityCountOriginal(1.504);
    runPositiveTestCase(1, 1);
  }

  @Test
  void updateTransaction_removeSecurityCountOriginal_badRequest() throws Exception {
    requestBody.setSecurityCountOriginal(null);
    runNegativeTestCase(1, 1, HttpStatus.BAD_REQUEST);
  }

  @Test
  void updateTransaction_updateSecurityCountSplitAdjusted_ok() throws Exception {
    requestBody.setSecurityCountSplitAdjusted(40.0);
    runPositiveTestCase(1, 1);
  }

  @Test
  void updateTransaction_updateSecurityCountSplitAdjusted_fractionalShares_ok() throws Exception {
    requestBody.setSecurityCountSplitAdjusted(30.586);
    runPositiveTestCase(1, 1);
  }

  @Test
  void updateTransaction_removeSecurityCountSplitAdjusted_ok() throws Exception {
    requestBody.setSecurityCountSplitAdjusted(null);
    runPositiveTestCase(1, 1);
  }

  @Test
  void updateTransaction_updateGrossValue_ok() throws Exception {
    requestBody.setGrossValue(5647.08);
    runPositiveTestCase(1, 1);
  }

  @Test
  void updateTransaction_removeGrossValue_badRequest() throws Exception {
    requestBody.setGrossValue(null);
    runNegativeTestCase(1, 1, HttpStatus.BAD_REQUEST);
  }

  @Test
  void updateTransaction_updateTax_ok() throws Exception {
    requestBody.setTax(12.34);
    runPositiveTestCase(1, 3);
  }

  @Test
  void updateTransaction_addTax_ok() throws Exception {
    requestBody.setTax(12.34);
    runPositiveTestCase(1, 1);
  }

  @Test
  void updateTransaction_removeTax_ok() throws Exception {
    requestBody.setTax(null);
    runPositiveTestCase(1, 3);
  }

  @Test
  void updateTransaction_updateFee_ok() throws Exception {
    requestBody.setFee(12.34);
    runPositiveTestCase(1, 1);
  }

  @Test
  void updateTransaction_addFee_ok() throws Exception {
    requestBody.setFee(12.34);
    runPositiveTestCase(1, 8);
  }

  @Test
  void updateTransaction_removeFee_ok() throws Exception {
    requestBody.setFee(null);
    runPositiveTestCase(1, 1);
  }

  @Test
  void updateTransaction_etfDepot_ok() throws Exception {
    requestBody = new TransactionUpdateDto();
    requestBody.setDate(LocalDate.of(2023, Month.NOVEMBER, 22));
    requestBody.setTime("12:54:05");
    requestBody.setSecurityId(SecurityIds.VNGGF);
    requestBody.setTransactionType(TransactionTypeDto.BUY);
    requestBody.setSecurityCountOriginal(9.294);
    requestBody.setSecurityCountSplitAdjusted(null);
    requestBody.setGrossValue(499.97);
    requestBody.setTax(null);
    requestBody.setFee(null);
    requestBody.setVersion(0L);

    runPositiveTestCase(4, 27);
  }

  private void runPositiveTestCase(long depotId, long transactionId) throws Exception {
    MvcResult mvcResult = putTransaction(depotId, transactionId).andExpect(status().isOk()).andReturn();
    TransactionReadDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), TransactionReadDto.class);
    verifyResponseBody(responseBody, transactionId);
    verifyDatabase(depotId, transactionId);
  }

  private void runNegativeTestCase(long depotId, long transactionId, HttpStatus expectedStatus) throws Exception {
    long transactionCount = transactionRepository.count();
    TransactionEntity beforeUpdate = transactionRepository.findById(transactionId).orElseThrow();
    entityManager.detach(beforeUpdate);
    long version = beforeUpdate.getVersion();

    MvcResult mvcResult =
        putTransaction(depotId, transactionId).andExpect(status().is(expectedStatus.value())).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();

    TransactionEntity afterUpdate = transactionRepository.findById(transactionId).orElseThrow();
    assertThat(afterUpdate.getVersion()).isEqualTo(version);
    assertTransactionUnchanged(beforeUpdate, afterUpdate);

    assertThat(transactionRepository.count()).isEqualTo(transactionCount);
  }

  private void assertTransactionUnchanged(TransactionEntity before, TransactionEntity after) {
    assertThat(after).usingRecursiveComparison().isEqualTo(before);
  }

  private ResultActions putTransaction(long depotId, long transactionId) throws Exception {
    return mockMvc.perform(put(String.format(ENDPOINT, depotId, transactionId)).contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestBody)));
  }

  private void verifyDatabase(long depotId, long transactionId) {
    TransactionEntity entity = transactionRepository.findById(transactionId).orElseThrow();
    assertThat(entity.getVersion()).isEqualTo(requestBody.getVersion() + 1);
    assertThat(entity.getDate()).isEqualTo(requestBody.getDate());

    if (requestBody.getTime() == null) {
      assertThat(entity.getTime()).isNull();
    } else {
      assertThat(entity.getTime()).isEqualTo(requestBody.getTime());
    }

    assertThat(entity.getDepotId()).isEqualTo(depotId);
    assertThat(entity.getSecurityId()).isEqualTo(requestBody.getSecurityId());
    assertThat(entity.getTransactionType().name()).isEqualTo(requestBody.getTransactionType().name());
    verifyBigDecimal(entity.getSecurityCountOriginal(), requestBody.getSecurityCountOriginal());
    verifyBigDecimal(entity.getSecurityCountSplitAdjusted(), requestBody.getSecurityCountSplitAdjusted());
    verifyBigDecimal(entity.getGrossValue(), requestBody.getGrossValue());
    verifyBigDecimal(entity.getTax(), requestBody.getTax());
    verifyBigDecimal(entity.getFee(), requestBody.getFee());
  }

  private void verifyResponseBody(TransactionReadDto responseBody, long expectedId) {
    assertThat(responseBody.getId()).isEqualTo(expectedId);
    assertThat(responseBody.getVersion()).isEqualTo(requestBody.getVersion() + 1);
    assertThat(responseBody.getDate()).isEqualTo(requestBody.getDate());
    assertThat(responseBody.getTime()).isEqualTo(requestBody.getTime());
    assertThat(responseBody.getSecurityId()).isEqualTo(requestBody.getSecurityId());
    assertThat(responseBody.getTransactionType()).isEqualTo(requestBody.getTransactionType());
    assertThat(responseBody.getSecurityCountOriginal()).isEqualTo(requestBody.getSecurityCountOriginal());
    assertThat(responseBody.getSecurityCountSplitAdjusted()).isEqualTo(requestBody.getSecurityCountSplitAdjusted());
    assertThat(responseBody.getGrossValue()).isEqualTo(requestBody.getGrossValue());
    assertThat(responseBody.getTax()).isEqualTo(requestBody.getTax());
    assertThat(responseBody.getFee()).isEqualTo(requestBody.getFee());
  }

  private void verifyBigDecimal(BigDecimal actual, Double expected) {
    if (expected == null) {
      assertThat(actual).isNull();
    } else {
      assertThat(actual.doubleValue()).isEqualByComparingTo(expected);
    }
  }
}
