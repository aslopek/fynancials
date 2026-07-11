package de.as.fynancials.depot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import integration.IntegrationTest;
import integration.sql.TestDataQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class DeleteDepotTest {

  private static final String ENDPOINT = "/depots/%d";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private DepotRepository depotRepository;


  @Test
  void deleteDepot_empty_ok() throws Exception {
    long depotCount = depotRepository.count();
    MvcResult mvcResult = deleteDepot(3L).andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(depotRepository.count()).isEqualTo(depotCount - 1);
    assertThat(depotRepository.existsById(3L)).isFalse();
  }

  @Test
  void deleteDepot_transactionsAreRemoved() throws Exception {
    long totalTransactionCount = TestDataQuery.getTransactionCount();
    long transactionCountDepot1 = TestDataQuery.getTransactionCountByDepotId(1);
    deleteDepot(1L).andExpect(status().isNoContent()).andReturn();
    assertThat(TestDataQuery.getTransactionCount()).isEqualTo(totalTransactionCount - transactionCountDepot1);
    assertThat(TestDataQuery.getTransactionCountByDepotId(1)).isZero();
  }

  @Test
  void deleteDepot_notFound() throws Exception {
    long depotCount = depotRepository.count();
    MvcResult mvcResult = deleteDepot(999L).andExpect(status().isNotFound()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(depotRepository.count()).isEqualTo(depotCount);
  }

  private ResultActions deleteDepot(long id) throws Exception {
    return mockMvc.perform(delete(String.format(ENDPOINT, id)));
  }
}
