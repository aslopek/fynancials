package de.as.fynancials.security.stocksplit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.as.fynancials.security.api.model.StockSplitDto;
import integration.IntegrationTest;
import integration.SecurityIds;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class GetStockSplitsTest {

  private static final String ENDPOINT = "/securities/%d/stock-splits";

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Autowired
  private MockMvc mockMvc;

  @Test
  void getStockSplits_ok() throws Exception {
    MvcResult mvcResult = getStockSplits(SecurityIds.NVDA).andExpect(status().isOk()).andReturn();
    List<StockSplitDto> responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});
    assertThat(responseBody).hasSize(5);

    StockSplitDto stockSplit = responseBody.get(0);
    assertThat(stockSplit.getExDate()).isEqualTo(LocalDate.of(2000, Month.JUNE, 27));
    assertThat(stockSplit.getQuantityOld()).isEqualTo(1);
    assertThat(stockSplit.getQuantityNew()).isEqualTo(2);

    stockSplit = responseBody.get(1);
    assertThat(stockSplit.getExDate()).isEqualTo(LocalDate.of(2001, Month.SEPTEMBER, 12));
    assertThat(stockSplit.getQuantityOld()).isEqualTo(1);
    assertThat(stockSplit.getQuantityNew()).isEqualTo(2);

    stockSplit = responseBody.get(2);
    assertThat(stockSplit.getExDate()).isEqualTo(LocalDate.of(2006, Month.APRIL, 7));
    assertThat(stockSplit.getQuantityOld()).isEqualTo(1);
    assertThat(stockSplit.getQuantityNew()).isEqualTo(2);

    stockSplit = responseBody.get(3);
    assertThat(stockSplit.getExDate()).isEqualTo(LocalDate.of(2007, Month.SEPTEMBER, 11));
    assertThat(stockSplit.getQuantityOld()).isEqualTo(2);
    assertThat(stockSplit.getQuantityNew()).isEqualTo(3);

    stockSplit = responseBody.get(4);
    assertThat(stockSplit.getExDate()).isEqualTo(LocalDate.of(2021, Month.JULY, 20));
    assertThat(stockSplit.getQuantityOld()).isEqualTo(1);
    assertThat(stockSplit.getQuantityNew()).isEqualTo(4);
  }

  @Test
  void getStockSplits_noStockSplitExists_noContent() throws Exception {
    MvcResult mvcResult = getStockSplits(SecurityIds.HAG).andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void getStockSplits_securityDoesNotExist_notFound() throws Exception {
    MvcResult mvcResult = getStockSplits(999).andExpect(status().isNotFound()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  private ResultActions getStockSplits(long id) throws Exception {
    return mockMvc.perform(get(String.format(ENDPOINT, id)));
  }
}
