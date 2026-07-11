package de.as.fynancials.depot.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.as.fynancials.depot.transaction.api.model.TransactionCreateDto;
import de.as.fynancials.depot.transaction.api.model.TransactionReadDto;
import de.as.fynancials.depot.transaction.api.model.TransactionTypeDto;
import integration.IntegrationTest;
import integration.SecurityIds;
import integration.sql.TestDataQuery;
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
class CreateTransactionTest {

  private static final String ENDPOINT = "/depots/%d/transactions";

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TransactionRepository transactionRepository;

  private TransactionCreateDto requestBody;

  @BeforeEach
  void beforeEach() {
    requestBody = new TransactionCreateDto();
    requestBody.setDate(LocalDate.of(2024, Month.JANUARY, 17));
    requestBody.setTime("16:10:38");
    requestBody.setSecurityId(SecurityIds.AAPL);
    requestBody.setTransactionType(TransactionTypeDto.BUY);
    requestBody.setSecurityCountOriginal(20.0);
    requestBody.setSecurityCountSplitAdjusted(null);
    requestBody.setGrossValue(3351.6);
    requestBody.setTax(null);
    requestBody.setFee(0.99);
  }

  @Test
  void addTransaction_buy_ok() throws Exception {
    runPositiveTestCase(1);
  }

  @Test
  void addTransaction_fractionalShares_ok() throws Exception {
    requestBody.setSecurityCountOriginal(19.908);
    runPositiveTestCase(1);
  }

  @Test
  void addTransaction_sell_ok() throws Exception {
    requestBody.setDate(LocalDate.of(2024, Month.JANUARY, 17));
    requestBody.setTime(null);
    requestBody.setSecurityId(SecurityIds.HAG);
    requestBody.setTransactionType(TransactionTypeDto.SELL);
    requestBody.setSecurityCountOriginal(150.0);
    requestBody.setSecurityCountSplitAdjusted(null);
    requestBody.setGrossValue(4264.5);
    requestBody.setTax(584.63);
    requestBody.setFee(0.99);
    runPositiveTestCase(2);
  }

  @Test
  void addTransaction_dividend_ok() throws Exception {
    requestBody.setDate(LocalDate.of(2024, Month.JANUARY, 17));
    requestBody.setTime(null);
    requestBody.setSecurityId(SecurityIds.NVDA);
    requestBody.setTransactionType(TransactionTypeDto.DIVIDEND);
    requestBody.setSecurityCountOriginal(30.0);
    requestBody.setSecurityCountSplitAdjusted(null);
    requestBody.setGrossValue(1.2);
    requestBody.setTax(0.32);
    requestBody.setFee(null);
    runPositiveTestCase(1);
  }

  @Test
  void addTransaction_specialDividend_ok() throws Exception {
    requestBody.setDate(LocalDate.of(2024, Month.JANUARY, 17));
    requestBody.setTime(null);
    requestBody.setSecurityId(SecurityIds.NVDA);
    requestBody.setTransactionType(TransactionTypeDto.SPECIAL_DIVIDEND);
    requestBody.setSecurityCountOriginal(30.0);
    requestBody.setSecurityCountSplitAdjusted(null);
    requestBody.setGrossValue(1.2);
    requestBody.setTax(0.32);
    requestBody.setFee(null);
    runPositiveTestCase(1);
  }

  @Test
  void addTransaction_tax_ok() throws Exception {
    requestBody.setDate(LocalDate.of(2024, Month.JANUARY, 17));
    requestBody.setTime(null);
    requestBody.setSecurityId(SecurityIds.AMZN);
    requestBody.setTransactionType(TransactionTypeDto.TAX);
    requestBody.setSecurityCountOriginal(40.0);
    requestBody.setSecurityCountSplitAdjusted(null);
    requestBody.setGrossValue(82.19);
    requestBody.setTax(null);
    requestBody.setFee(null);
    runPositiveTestCase(1);
  }

  @Test
  void addTransaction_depotDoesNotExist_badRequest() throws Exception {
    runNegativeTestCase(999, HttpStatus.BAD_REQUEST);
  }

  @Test
  void addTransaction_dateIsNull_badRequest() throws Exception {
    requestBody.setDate(null);
    runNegativeTestCase(1, HttpStatus.BAD_REQUEST);
  }

  @Test
  void addTransaction_securityIdIsNull_badRequest() throws Exception {
    requestBody.setSecurityId(null);
    runNegativeTestCase(1, HttpStatus.BAD_REQUEST);
  }

  @Test
  void addTransaction_securityIdDoesNotExist_badRequest() throws Exception {
    requestBody.setSecurityId(999L);
    runNegativeTestCase(1, HttpStatus.BAD_REQUEST);
  }

  @Test
  void addTransaction_typeIsNull_badRequest() throws Exception {
    requestBody.setTransactionType(null);
    runNegativeTestCase(1, HttpStatus.BAD_REQUEST);
  }

  @Test
  void addTransaction_securityCountOriginalIsNull() throws Exception {
    requestBody.setSecurityCountOriginal(null);
    runNegativeTestCase(1, HttpStatus.BAD_REQUEST);
  }

  @Test
  void addTransaction_grossValueIsNull_badRequest() throws Exception {
    requestBody.setGrossValue(null);
    runNegativeTestCase(1, HttpStatus.BAD_REQUEST);
  }

  private void runPositiveTestCase(long depotId) throws Exception {
    long numberOfTransactions = transactionRepository.count();
    MvcResult mvcResult = postTransaction(depotId).andExpect(status().isCreated()).andReturn();

    // verify response
    TransactionReadDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), TransactionReadDto.class);
    verifyResponseBody(responseBody);
    verifyLocationHeader(mvcResult, depotId);

    // verify database
    assertThat(transactionRepository.count()).isEqualTo(numberOfTransactions + 1);
    verifyDatabase(depotId, responseBody.getId());
  }

  private void runNegativeTestCase(long depotId, HttpStatus expectedStatus) throws Exception {
    long numberOfTransactions = transactionRepository.count();
    long numberOfDepots = TestDataQuery.getDepotCount();
    MvcResult mvcResult = postTransaction(depotId).andExpect(status().is(expectedStatus.value())).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(mvcResult.getResponse().getHeader("Location")).isNull();
    assertThat(transactionRepository.count()).isEqualTo(numberOfTransactions);
    assertThat(TestDataQuery.getDepotCount()).isEqualTo(numberOfDepots);
  }

  private ResultActions postTransaction(long depotId) throws Exception {
    return mockMvc.perform(post(String.format(ENDPOINT, depotId)).contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestBody)));
  }

  private void verifyDatabase(long depotId, long expectedTransactionId) {
    TransactionEntity entity = transactionRepository.findById(expectedTransactionId).orElseThrow();
    assertThat(entity.getVersion()).isZero();
    assertThat(entity.getDate()).isEqualTo(requestBody.getDate());

    if (requestBody.getTime() == null) {
      assertThat(entity.getTime()).isNull();
    } else {
      assertThat(entity.getTime()).isEqualTo(requestBody.getTime());
    }

    assertThat(entity.getDepotId()).isEqualTo(depotId);
    assertThat(entity.getSecurityId()).isEqualTo(requestBody.getSecurityId());
    assertThat(entity.getTransactionType()).isEqualTo(requestBody.getTransactionType());
    verifyBigDecimal(entity.getSecurityCountOriginal(), requestBody.getSecurityCountOriginal());
    verifyBigDecimal(entity.getSecurityCountSplitAdjusted(), requestBody.getSecurityCountSplitAdjusted());
    verifyBigDecimal(entity.getGrossValue(), requestBody.getGrossValue());
    verifyBigDecimal(entity.getTax(), requestBody.getTax());
    verifyBigDecimal(entity.getFee(), requestBody.getFee());
  }

  private void verifyResponseBody(TransactionReadDto responseBody) {
    assertThat(responseBody.getId()).isGreaterThan(0);
    assertThat(responseBody.getVersion()).isZero();
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

  private void verifyLocationHeader(MvcResult mvcResult, long depotId) throws Exception {
    String locationHeader = mvcResult.getResponse().getHeader("Location");
    TransactionReadDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), TransactionReadDto.class);
    assertThat(locationHeader).isEqualTo(String.format(ENDPOINT, depotId) + "/" + responseBody.getId());
  }

  private void verifyBigDecimal(BigDecimal actual, Double expected) {
    if (expected == null) {
      assertThat(actual).isNull();
    } else {
      assertThat(actual.doubleValue()).isEqualByComparingTo(expected);
    }
  }
}
