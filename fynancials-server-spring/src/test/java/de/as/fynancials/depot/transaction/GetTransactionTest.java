package de.as.fynancials.depot.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.as.fynancials.depot.transaction.api.model.TransactionReadDto;
import de.as.fynancials.depot.transaction.api.model.TransactionTypeDto;
import integration.IntegrationTest;
import integration.SecurityIds;
import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class GetTransactionTest {

  private static final String ENDPOINT = "/depots/%d/transactions/%d";

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Autowired
  private MockMvc mockMvc;

  @Test
  void getTransaction_buy_ok() throws Exception {
    MvcResult mvcResult = getTransaction(1, 6).andExpect(status().isOk()).andReturn();
    TransactionReadDto transaction =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), TransactionReadDto.class);
    assertThat(transaction.getDate()).isEqualTo(LocalDate.of(2020, Month.NOVEMBER, 16));
    assertThat(transaction.getTime()).isEqualTo("08:21:19");
    assertThat(transaction.getSecurityId()).isEqualTo(SecurityIds.NVDA);
    assertThat(transaction.getTransactionType()).isEqualTo(TransactionTypeDto.BUY);
    assertThat(transaction.getSecurityCountOriginal()).isEqualTo(2.16);
    assertThat(transaction.getSecurityCountSplitAdjusted()).isEqualTo(8.64);
    assertThat(transaction.getGrossValue()).isEqualTo(984.64);
    assertThat(transaction.getTax()).isNull();
    assertThat(transaction.getFee()).isEqualTo(10);
    assertThat(transaction.getVersion()).isEqualTo(1);
    assertThat(transaction.getId()).isEqualTo(6);
  }

  @Test
  void getTransaction_sell_ok() throws Exception {
    MvcResult mvcResult = getTransaction(1, 11).andExpect(status().isOk()).andReturn();
    TransactionReadDto transaction =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), TransactionReadDto.class);
    assertThat(transaction.getDate()).isEqualTo(LocalDate.of(2021, Month.APRIL, 13));
    assertThat(transaction.getTime()).isEqualTo("11:48:34");
    assertThat(transaction.getSecurityId()).isEqualTo(SecurityIds.HAG);
    assertThat(transaction.getTransactionType()).isEqualTo(TransactionTypeDto.SELL);
    assertThat(transaction.getSecurityCountOriginal()).isEqualTo(120);
    assertThat(transaction.getSecurityCountSplitAdjusted()).isEqualTo(null);
    assertThat(transaction.getGrossValue()).isEqualTo(1764);
    assertThat(transaction.getTax()).isEqualTo(6.42);
    assertThat(transaction.getFee()).isEqualTo(10);
    assertThat(transaction.getVersion()).isEqualTo(0);
    assertThat(transaction.getId()).isEqualTo(11);
  }

  @Test
  void getTransaction_dividend_ok() throws Exception {
    MvcResult mvcResult = getTransaction(2, 23).andExpect(status().isOk()).andReturn();
    TransactionReadDto transaction =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), TransactionReadDto.class);
    assertThat(transaction.getDate()).isEqualTo(LocalDate.of(2023, Month.MAY, 17));
    assertThat(transaction.getTime()).isNull();
    assertThat(transaction.getSecurityId()).isEqualTo(SecurityIds.HAG);
    assertThat(transaction.getTransactionType()).isEqualTo(TransactionTypeDto.DIVIDEND);
    assertThat(transaction.getSecurityCountOriginal()).isEqualTo(250);
    assertThat(transaction.getSecurityCountSplitAdjusted()).isEqualTo(null);
    assertThat(transaction.getGrossValue()).isEqualTo(75);
    assertThat(transaction.getTax()).isNull();
    assertThat(transaction.getFee()).isNull();
    assertThat(transaction.getVersion()).isEqualTo(0);
    assertThat(transaction.getId()).isEqualTo(23);
  }

  @Test
  void getTransaction_specialDividend_ok() throws Exception {
    MvcResult mvcResult = getTransaction(1, 31).andExpect(status().isOk()).andReturn();
    TransactionReadDto transaction =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), TransactionReadDto.class);
    assertThat(transaction.getDate()).isEqualTo(LocalDate.of(2023, Month.DECEMBER, 6));
    assertThat(transaction.getTime()).isNull();
    assertThat(transaction.getSecurityId()).isEqualTo(SecurityIds.LVMH);
    assertThat(transaction.getTransactionType()).isEqualTo(TransactionTypeDto.SPECIAL_DIVIDEND);
    assertThat(transaction.getSecurityCountOriginal()).isEqualTo(4.25);
    assertThat(transaction.getSecurityCountSplitAdjusted()).isEqualTo(null);
    assertThat(transaction.getGrossValue()).isEqualTo(21.68);
    assertThat(transaction.getTax()).isEqualTo(5.72);
    assertThat(transaction.getFee()).isNull();
    assertThat(transaction.getVersion()).isEqualTo(0);
    assertThat(transaction.getId()).isEqualTo(31);
  }

  @Test
  void getTransaction_tax_ok() throws Exception {
    MvcResult mvcResult = getTransaction(4, 30).andExpect(status().isOk()).andReturn();
    TransactionReadDto transaction =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), TransactionReadDto.class);
    assertThat(transaction.getDate()).isEqualTo(LocalDate.of(2024, Month.JANUARY, 2));
    assertThat(transaction.getTime()).isNull();
    assertThat(transaction.getSecurityId()).isEqualTo(SecurityIds.VNGGF);
    assertThat(transaction.getTransactionType()).isEqualTo(TransactionTypeDto.TAX);
    assertThat(transaction.getSecurityCountOriginal()).isEqualTo(34.318);
    assertThat(transaction.getSecurityCountSplitAdjusted()).isEqualTo(null);
    assertThat(transaction.getGrossValue()).isEqualTo(7.89);
    assertThat(transaction.getTax()).isNull();
    assertThat(transaction.getFee()).isNull();
    assertThat(transaction.getVersion()).isEqualTo(0);
    assertThat(transaction.getId()).isEqualTo(30);
  }

  @Test
  void getTransaction_transactionIdDoesNotExist_notFound() throws Exception {
    MvcResult mvcResult = getTransaction(1, 999).andExpect(status().isNotFound()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void getTransaction_depotIdDoesNotExist_notFound() throws Exception {
    MvcResult mvcResult = getTransaction(999, 6).andExpect(status().isNotFound()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void getTransaction_depotIdAndTransactionIdDoNotExist_notFound() throws Exception {
    MvcResult mvcResult = getTransaction(999, 999).andExpect(status().isNotFound()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void getTransaction_transactionIsAssignedToOtherDepot_notFound() throws Exception {
    MvcResult mvcResult = getTransaction(2, 6).andExpect(status().isNotFound()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  private ResultActions getTransaction(long depotId, long transactionId) throws Exception {
    String url = String.format(ENDPOINT, depotId, transactionId);
    return mockMvc.perform(get(url));
  }
}
