package de.as.fynancials.depot.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import integration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class DeleteDepotTest {

  private static final String ENDPOINT = "/depots/%d/transactions";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TransactionRepository transactionRepository;

  @Test
  void deleteTransaction_ok() throws Exception {
    long transactionCount = transactionRepository.count();
    long depotId = 2;
    long transactionId = 18;
    MvcResult mvcResult = deleteTransaction(depotId, transactionId).andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(transactionRepository.count()).isEqualTo(transactionCount - 1);
    assertThat(transactionRepository.existsByIdAndDepotId(transactionId, depotId)).isFalse();
  }

  @Test
  void deleteTransaction_transactionDoesNotExist_notFound() throws Exception {
    long transactionCount = transactionRepository.count();
    long depotId = 2;
    long transactionId = 999;
    MvcResult mvcResult = deleteTransaction(depotId, transactionId).andExpect(status().isNotFound()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(transactionRepository.count()).isEqualTo(transactionCount);
  }

  @Test
  void deleteTransaction_depotDoesNotExist_notFound() throws Exception {
    long transactionCount = transactionRepository.count();
    long depotId = 999;
    long transactionId = 18;
    MvcResult mvcResult = deleteTransaction(depotId, transactionId).andExpect(status().isNotFound()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(transactionRepository.count()).isEqualTo(transactionCount);
  }

  @Test
  void deleteTransaction_transactionIsAssignedToOtherDepot_notFound() throws Exception {
    long transactionCount = transactionRepository.count();
    long depotId = 1;
    long transactionId = 18;
    MvcResult mvcResult = deleteTransaction(depotId, transactionId).andExpect(status().isNotFound()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(transactionRepository.count()).isEqualTo(transactionCount);
  }

  private ResultActions deleteTransaction(long depotId, long transactionId) throws Exception {
    return mockMvc.perform(delete(String.format(ENDPOINT, depotId) + "/" + transactionId));
  }
}
