package de.as.fynancials.security.stocksplit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.as.fynancials.depot.transaction.Transaction;
import de.as.fynancials.price.security.historical.HistoricalSecurityPrice;
import de.as.fynancials.security.api.model.StockSplitDto;
import integration.IntegrationTest;
import integration.SecurityIds;
import integration.sql.TestDataQuery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class CreateStockSplitTest {

  private static final String ENDPOINT = "/securities/%d/stock-splits";

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private StockSplitRepository stockSplitRepository;

  private StockSplitDto requestBody;

  @BeforeEach
  void beforeEach() {
    requestBody = new StockSplitDto();
    requestBody.setExDate(LocalDate.of(2023, Month.DECEMBER, 15));
    requestBody.setQuantityOld(2L);
    requestBody.setQuantityNew(5L);
  }

  @Test
  void createStockSplit_doNotUpdateAnything_ok() throws Exception {
    long count = stockSplitRepository.count();
    MvcResult mvcResult = createStockSplit(SecurityIds.AMZN, false, false).andExpect(status().isCreated()).andReturn();

    // verify response
    String locationHeader = mvcResult.getResponse().getHeader("Location");
    assertThat(locationHeader).isEqualTo(String.format(ENDPOINT, SecurityIds.AMZN));
    StockSplitDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), StockSplitDto.class);
    assertThat(responseBody).isEqualTo(requestBody);

    // verify database
    assertThat(stockSplitRepository.count()).isEqualTo(count + 1);
    List<StockSplitEntity> stockSplits = stockSplitRepository.findAllBySecurityIdOrderByExDateAsc(SecurityIds.AMZN);
    assertThat(stockSplits).hasSize(5);
    StockSplitEntity newStockSplit = stockSplits.get(4);
    assertThat(newStockSplit.getExDate()).isEqualTo(requestBody.getExDate());
    assertThat(newStockSplit.getQuantityOld()).isEqualTo(requestBody.getQuantityOld());
    assertThat(newStockSplit.getQuantityNew()).isEqualTo(requestBody.getQuantityNew());
  }

  @Test
  void createStockSplit_updateTransactions_ok() throws Exception {
    long count = TestDataQuery.getTransactionCount();
    createStockSplit(SecurityIds.NVDA, true, false).andExpect(status().isCreated()).andReturn();

    assertThat(TestDataQuery.getTransactionCount()).isEqualTo(count);
    Transaction transaction = TestDataQuery.getTransaction(2);
    assertThat(transaction.getVersion()).isEqualTo(2);
    assertThat(transaction.getSecurityCountOriginal()).isEqualByComparingTo(BigDecimal.valueOf(2.16));
    assertThat(transaction.getSecurityCountSplitAdjusted()).isEqualByComparingTo(BigDecimal.valueOf(21.6));
    assertThat(transaction.getGrossValue()).isEqualByComparingTo(BigDecimal.valueOf(926.21));
    assertThat(transaction.getTax()).isNull();
    assertThat(transaction.getFee()).isEqualByComparingTo(BigDecimal.valueOf(10.0));

    transaction = TestDataQuery.getTransaction(15);
    assertThat(transaction.getVersion()).isEqualTo(1);
    assertThat(transaction.getSecurityCountOriginal()).isEqualByComparingTo(BigDecimal.valueOf(12.96));
    assertThat(transaction.getSecurityCountSplitAdjusted()).isEqualByComparingTo(BigDecimal.valueOf(32.4));
    assertThat(transaction.getGrossValue()).isEqualByComparingTo(BigDecimal.valueOf(2332.8));
    assertThat(transaction.getTax()).isNull();
    assertThat(transaction.getFee()).isEqualByComparingTo(BigDecimal.valueOf(10.0));
  }

  @Test
  void createStockSplit_updateHistoricalPrices_ok() throws Exception {
    long count = TestDataQuery.getHistoricalSecurityPriceCountBySecurityId(SecurityIds.AMZN);
    createStockSplit(SecurityIds.AMZN, false, true).andExpect(status().isCreated()).andReturn();

    assertThat(TestDataQuery.getHistoricalSecurityPriceCountBySecurityId(SecurityIds.AMZN)).isEqualTo(count);
    HistoricalSecurityPrice price =
        TestDataQuery.getHistoricalSecurityPrice(SecurityIds.AMZN, LocalDate.of(2023, Month.DECEMBER, 14));
    assertThat(price.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(53.816));
    price = TestDataQuery.getHistoricalSecurityPrice(SecurityIds.AMZN, LocalDate.of(2023, Month.DECEMBER, 15));
    assertThat(price.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(136.66));
  }

  @Test
  void createStockSplit_securityDoesNotExist_notFound() throws Exception {
    long count = stockSplitRepository.count();
    MvcResult mvcResult = createStockSplit(999, false, false).andExpect(status().isNotFound()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(stockSplitRepository.count()).isEqualTo(count);
  }

  @Test
  void createStockSplit_exDateIsToday_badRequest() throws Exception {
    long count = stockSplitRepository.count();
    requestBody.setExDate(LocalDate.now());
    MvcResult mvcResult =
        createStockSplit(SecurityIds.AMZN, false, false).andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(stockSplitRepository.count()).isEqualTo(count);
  }

  @Test
  void createStockSplit_exDateIsInTheFuture_badRequest() throws Exception {
    long count = stockSplitRepository.count();
    requestBody.setExDate(LocalDate.now().plusDays(1));
    MvcResult mvcResult =
        createStockSplit(SecurityIds.AMZN, false, false).andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(stockSplitRepository.count()).isEqualTo(count);
  }

  @Test
  void createStockSplit_anotherStockSplitExistsAfterwards_badRequest() throws Exception {
    long count = stockSplitRepository.count();
    requestBody.setExDate(LocalDate.of(2022, Month.JUNE, 5));
    MvcResult mvcResult =
        createStockSplit(SecurityIds.AMZN, false, false).andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(stockSplitRepository.count()).isEqualTo(count);
  }

  @Test
  void createStockSplit_anotherStockSplitExistsOnTheSameDate_conflict() throws Exception {
    long count = stockSplitRepository.count();
    requestBody.setExDate(LocalDate.of(2022, Month.JUNE, 6));
    MvcResult mvcResult = createStockSplit(SecurityIds.AMZN, false, false).andExpect(status().isConflict()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(stockSplitRepository.count()).isEqualTo(count);
  }

  private ResultActions createStockSplit(long id, boolean updateTransactions, boolean updateHistoricalPrices)
      throws Exception {
    return mockMvc.perform(
        post(String.format(ENDPOINT, id)).queryParam("updateTransactions", Boolean.toString(updateTransactions))
            .queryParam("updateHistoricalPrices", Boolean.toString(updateHistoricalPrices))
            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestBody)));
  }
}
