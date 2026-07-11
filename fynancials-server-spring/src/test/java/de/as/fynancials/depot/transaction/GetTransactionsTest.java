package de.as.fynancials.depot.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.as.fynancials.depot.transaction.api.model.PaginatedTransactionReadDto;
import de.as.fynancials.depot.transaction.api.model.TransactionReadDto;
import de.as.fynancials.depot.transaction.api.model.TransactionTypeDto;
import integration.DepotIds;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@IntegrationTest
class GetTransactionsTest {

  private static final String ENDPOINT = "/depots/%d/transactions";

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Autowired
  private MockMvc mockMvc;

  @Test
  void getTransactions_emptyDepot_noContent() throws Exception {
    MvcResult mvcResult = getSecurities(DepotIds.EMPTY_DEPOT, null, null).andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void getAllTransactions_firstDepot_defaultPage_ok() throws Exception {
    long depotId = DepotIds.FIRST_DEPOT;
    MvcResult mvcResult = getSecurities(depotId, null, null).andExpect(status().isOk()).andReturn();
    PaginatedTransactionReadDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PaginatedTransactionReadDto.class);

    assertThat(responseBody.getTotal()).isEqualTo(21);
    assertThat(responseBody.getCurrentPage()).isZero();
    assertThat(responseBody.getLastPage()).isEqualTo(2);
    assertThat(responseBody.getPageSize()).isEqualTo(10);
    verifyTransactionIds(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 10L, 11L), responseBody.getItems());

    TransactionReadDto transaction = new TransactionReadDto();
    transaction.setId(1L);
    transaction.setDate(LocalDate.of(2020, Month.JULY, 13));
    transaction.setTime("21:09:34");
    transaction.setSecurityId(SecurityIds.AMZN);
    transaction.setTransactionType(TransactionTypeDto.BUY);
    transaction.setSecurityCountOriginal(1.35);
    transaction.setSecurityCountSplitAdjusted(27.0);
    transaction.setGrossValue(3789.45);
    transaction.setTax(null);
    transaction.setFee(10.0);
    transaction.setNetValue(3799.45);
    transaction.setVersion(1L);
    assertThat(responseBody.getItems().get(0)).isEqualTo(transaction);

    transaction = new TransactionReadDto();
    transaction.setId(3L);
    transaction.setDate(LocalDate.of(2020, Month.NOVEMBER, 5));
    transaction.setTime("16:19:17");
    transaction.setSecurityId(SecurityIds.NVDA);
    transaction.setTransactionType(TransactionTypeDto.SELL);
    transaction.setSecurityCountOriginal(2.16);
    transaction.setSecurityCountSplitAdjusted(8.64);
    transaction.setGrossValue(1040.04);
    transaction.setTax(23.35);
    transaction.setFee(10.0);
    transaction.setNetValue(1006.69);
    transaction.setVersion(1L);
    assertThat(responseBody.getItems().get(2)).isEqualTo(transaction);

    transaction = new TransactionReadDto();
    transaction.setId(8L);
    transaction.setDate(LocalDate.of(2020, Month.DECEMBER, 31));
    transaction.setTime(null);
    transaction.setSecurityId(SecurityIds.NVDA);
    transaction.setTransactionType(TransactionTypeDto.DIVIDEND);
    transaction.setSecurityCountOriginal(2.16);
    transaction.setSecurityCountSplitAdjusted(8.64);
    transaction.setGrossValue(0.28);
    transaction.setTax(0.07);
    transaction.setFee(null);
    transaction.setNetValue(0.21);
    transaction.setVersion(1L);
    assertThat(responseBody.getItems().get(7)).isEqualTo(transaction);
  }

  @Test
  void getFirstDepot_page2_ok() throws Exception {
    long depotId = DepotIds.FIRST_DEPOT;
    MvcResult mvcResult = getSecurities(depotId, 1, null).andExpect(status().isOk()).andReturn();
    PaginatedTransactionReadDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PaginatedTransactionReadDto.class);

    assertThat(responseBody.getTotal()).isEqualTo(21);
    assertThat(responseBody.getCurrentPage()).isOne();
    assertThat(responseBody.getLastPage()).isEqualTo(2);
    assertThat(responseBody.getPageSize()).isEqualTo(10);
    verifyTransactionIds(List.of(12L, 14L, 15L, 16L, 19L, 39L, 24L, 25L, 26L, 31L), responseBody.getItems());

    TransactionReadDto transaction = new TransactionReadDto();
    transaction.setId(31L);
    transaction.setDate(LocalDate.of(2023, Month.DECEMBER, 6));
    transaction.setTime(null);
    transaction.setSecurityId(SecurityIds.LVMH);
    transaction.setTransactionType(TransactionTypeDto.SPECIAL_DIVIDEND);
    transaction.setSecurityCountOriginal(4.25);
    transaction.setSecurityCountSplitAdjusted(null);
    transaction.setGrossValue(21.68);
    transaction.setTax(5.72);
    transaction.setFee(null);
    transaction.setNetValue(15.96);
    transaction.setVersion(0L);
    assertThat(responseBody.getItems().get(9)).isEqualTo(transaction);
  }

  @Test
  void getFirstDepot_largerPageSize_ok() throws Exception {
    long depotId = DepotIds.FIRST_DEPOT;
    int pageSize = 18;
    MvcResult mvcResult = getSecurities(depotId, null, pageSize).andExpect(status().isOk()).andReturn();
    PaginatedTransactionReadDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PaginatedTransactionReadDto.class);

    assertThat(responseBody.getTotal()).isEqualTo(21);
    assertThat(responseBody.getCurrentPage()).isZero();
    assertThat(responseBody.getLastPage()).isOne();
    assertThat(responseBody.getPageSize()).isEqualTo(pageSize);
    verifyTransactionIds(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 10L, 11L, 12L, 14L, 15L, 16L, 19L, 39L, 24L, 25L),
        responseBody.getItems());

    TransactionReadDto transaction = new TransactionReadDto();
    transaction.setId(25L);
    transaction.setDate(LocalDate.of(2023, Month.NOVEMBER, 27));
    transaction.setTime("13:26:43");
    transaction.setSecurityId(SecurityIds.LVMH);
    transaction.setTransactionType(TransactionTypeDto.BUY);
    transaction.setSecurityCountOriginal(2.55);
    transaction.setSecurityCountSplitAdjusted(null);
    transaction.setGrossValue(1791.38);
    transaction.setTax(5.37);
    transaction.setFee(10.0);
    transaction.setNetValue(1806.75);
    transaction.setVersion(0L);
    assertThat(responseBody.getItems().get(17)).isEqualTo(transaction);
  }

  @Test
  void getFirstDepot_allTransactionsInOnePage_ok() throws Exception {
    long depotId = DepotIds.FIRST_DEPOT;
    int pageSize = 50;
    MvcResult mvcResult = getSecurities(depotId, null, pageSize).andExpect(status().isOk()).andReturn();
    PaginatedTransactionReadDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PaginatedTransactionReadDto.class);

    assertThat(responseBody.getTotal()).isEqualTo(21);
    assertThat(responseBody.getCurrentPage()).isZero();
    assertThat(responseBody.getLastPage()).isZero();
    assertThat(responseBody.getPageSize()).isEqualTo(pageSize);
    verifyTransactionIds(
        List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 10L, 11L, 12L, 14L, 15L, 16L, 19L, 39L, 24L, 25L, 26L, 31L, 32L),
        responseBody.getItems());

    TransactionReadDto transaction = new TransactionReadDto();
    transaction.setId(32L);
    transaction.setDate(LocalDate.of(2024, Month.JANUARY, 2));
    transaction.setTime(null);
    transaction.setSecurityId(SecurityIds.NVDA);
    transaction.setTransactionType(TransactionTypeDto.SPECIAL_DIVIDEND);
    transaction.setSecurityCountOriginal(21.6);
    transaction.setSecurityCountSplitAdjusted(null);
    transaction.setGrossValue(8.64);
    transaction.setTax(2.28);
    transaction.setFee(null);
    transaction.setNetValue(6.36);
    transaction.setVersion(0L);
    assertThat(responseBody.getItems().get(20)).isEqualTo(transaction);
  }

  @Test
  void getFirstDepot_smallerPageSize_ok() throws Exception {
    long depotId = DepotIds.FIRST_DEPOT;
    int pageSize = 3;
    MvcResult mvcResult = getSecurities(depotId, null, pageSize).andExpect(status().isOk()).andReturn();
    PaginatedTransactionReadDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PaginatedTransactionReadDto.class);

    assertThat(responseBody.getTotal()).isEqualTo(21);
    assertThat(responseBody.getCurrentPage()).isZero();
    assertThat(responseBody.getLastPage()).isEqualTo(6);
    assertThat(responseBody.getPageSize()).isEqualTo(pageSize);
    verifyTransactionIds(List.of(1L, 2L, 3L), responseBody.getItems());

    TransactionReadDto transaction = new TransactionReadDto();
    transaction.setId(1L);
    transaction.setDate(LocalDate.of(2020, Month.JULY, 13));
    transaction.setTime("21:09:34");
    transaction.setSecurityId(SecurityIds.AMZN);
    transaction.setTransactionType(TransactionTypeDto.BUY);
    transaction.setSecurityCountOriginal(1.35);
    transaction.setSecurityCountSplitAdjusted(27.0);
    transaction.setGrossValue(3789.45);
    transaction.setTax(null);
    transaction.setFee(10.0);
    transaction.setNetValue(3799.45);
    transaction.setVersion(1L);
    assertThat(responseBody.getItems().get(0)).isEqualTo(transaction);
  }

  @Test
  void getFirstDepot_specifyPageNumberAndSize_ok() throws Exception {
    long depotId = DepotIds.FIRST_DEPOT;
    int page = 3;
    int pageSize = 4;
    MvcResult mvcResult = getSecurities(depotId, page, pageSize).andExpect(status().isOk()).andReturn();
    PaginatedTransactionReadDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PaginatedTransactionReadDto.class);

    assertThat(responseBody.getTotal()).isEqualTo(21);
    assertThat(responseBody.getCurrentPage()).isEqualTo(page);
    assertThat(responseBody.getLastPage()).isEqualTo(5);
    assertThat(responseBody.getPageSize()).isEqualTo(pageSize);
    verifyTransactionIds(List.of(15L, 16L, 19L, 39L), responseBody.getItems());

    TransactionReadDto transaction = new TransactionReadDto();
    transaction.setId(16L);
    transaction.setDate(LocalDate.of(2021, Month.SEPTEMBER, 27));
    transaction.setTime(null);
    transaction.setSecurityId(SecurityIds.NVDA);
    transaction.setTransactionType(TransactionTypeDto.DIVIDEND);
    transaction.setSecurityCountOriginal(21.6);
    transaction.setSecurityCountSplitAdjusted(null);
    transaction.setGrossValue(0.73);
    transaction.setTax(0.19);
    transaction.setFee(null);
    transaction.setNetValue(0.54);
    transaction.setVersion(0L);
    assertThat(responseBody.getItems().get(1)).isEqualTo(transaction);
  }

  @Test
  void getEtfDepot_defaultPage_ok() throws Exception {
    long depotId = DepotIds.ETF_DEPOT;
    MvcResult mvcResult = getSecurities(depotId, null, null).andExpect(status().isOk()).andReturn();
    PaginatedTransactionReadDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PaginatedTransactionReadDto.class);

    assertThat(responseBody.getTotal()).isEqualTo(4);
    assertThat(responseBody.getCurrentPage()).isZero();
    assertThat(responseBody.getLastPage()).isZero();
    assertThat(responseBody.getPageSize()).isEqualTo(10);
    verifyTransactionIds(List.of(27L, 28L, 30L, 29L), responseBody.getItems());

    TransactionReadDto transaction = new TransactionReadDto();
    transaction.setId(28L);
    transaction.setDate(LocalDate.of(2023, Month.DECEMBER, 22));
    transaction.setTime("12:44:46");
    transaction.setSecurityId(SecurityIds.VNGGF);
    transaction.setTransactionType(TransactionTypeDto.BUY);
    transaction.setSecurityCountOriginal(16.849);
    transaction.setSecurityCountSplitAdjusted(null);
    transaction.setGrossValue(949.96);
    transaction.setTax(null);
    transaction.setFee(null);
    transaction.setNetValue(949.96);
    transaction.setVersion(0L);
    assertThat(responseBody.getItems().get(1)).isEqualTo(transaction);

    transaction = new TransactionReadDto();
    transaction.setId(30L);
    transaction.setDate(LocalDate.of(2024, Month.JANUARY, 2));
    transaction.setTime(null);
    transaction.setSecurityId(SecurityIds.VNGGF);
    transaction.setTransactionType(TransactionTypeDto.TAX);
    transaction.setSecurityCountOriginal(34.318);
    transaction.setSecurityCountSplitAdjusted(null);
    transaction.setGrossValue(7.89);
    transaction.setTax(null);
    transaction.setFee(null);
    transaction.setNetValue(7.89);
    transaction.setVersion(0L);
    assertThat(responseBody.getItems().get(2)).isEqualTo(transaction);
  }

  @Test
  void getFirstDepot_sellTransactions_defaultPage_ok() throws Exception {
    long depotId = DepotIds.FIRST_DEPOT;
    List<TransactionTypeDto> transactionTypes = List.of(TransactionTypeDto.SELL);
    MvcResult mvcResult = getSecurities(depotId, null, null, transactionTypes).andExpect(status().isOk()).andReturn();
    PaginatedTransactionReadDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PaginatedTransactionReadDto.class);

    assertThat(responseBody.getTotal()).isEqualTo(3);
    assertThat(responseBody.getCurrentPage()).isZero();
    assertThat(responseBody.getLastPage()).isZero();
    assertThat(responseBody.getPageSize()).isEqualTo(10);
    verifyTransactionIds(List.of(3L, 4L, 11L), responseBody.getItems());

    TransactionReadDto transaction = new TransactionReadDto();
    transaction.setId(11L);
    transaction.setDate(LocalDate.of(2021, Month.APRIL, 13));
    transaction.setTime("11:48:34");
    transaction.setSecurityId(SecurityIds.HAG);
    transaction.setTransactionType(TransactionTypeDto.SELL);
    transaction.setSecurityCountOriginal(120.0);
    transaction.setSecurityCountSplitAdjusted(null);
    transaction.setGrossValue(1764.0);
    transaction.setTax(6.42);
    transaction.setFee(10.0);
    transaction.setNetValue(1747.58);
    transaction.setVersion(0L);
    assertThat(responseBody.getItems().get(2)).isEqualTo(transaction);
  }

  @Test
  void getFirstDepot_sellAndDividendTransactions_defaultPage_ok() throws Exception {
    long depotId = DepotIds.FIRST_DEPOT;
    List<TransactionTypeDto> transactionTypes = List.of(TransactionTypeDto.SELL, TransactionTypeDto.DIVIDEND);
    MvcResult mvcResult = getSecurities(depotId, null, null, transactionTypes).andExpect(status().isOk()).andReturn();
    PaginatedTransactionReadDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PaginatedTransactionReadDto.class);

    assertThat(responseBody.getTotal()).isEqualTo(10);
    assertThat(responseBody.getCurrentPage()).isZero();
    assertThat(responseBody.getLastPage()).isZero();
    assertThat(responseBody.getPageSize()).isEqualTo(10);
    verifyTransactionIds(List.of(3L, 4L, 8L, 10L, 11L, 14L, 16L, 19L, 39L, 26L), responseBody.getItems());

    TransactionReadDto transaction = new TransactionReadDto();
    transaction.setId(8L);
    transaction.setDate(LocalDate.of(2020, Month.DECEMBER, 31));
    transaction.setTime(null);
    transaction.setSecurityId(SecurityIds.NVDA);
    transaction.setTransactionType(TransactionTypeDto.DIVIDEND);
    transaction.setSecurityCountOriginal(2.16);
    transaction.setSecurityCountSplitAdjusted(8.64);
    transaction.setGrossValue(0.28);
    transaction.setTax(0.07);
    transaction.setFee(null);
    transaction.setNetValue(0.21);
    transaction.setVersion(1L);
    assertThat(responseBody.getItems().get(2)).isEqualTo(transaction);
  }

  @Test
  void getFirstDepot_sellAndDividendTransactions_specificPage_ok() throws Exception {
    long depotId = DepotIds.FIRST_DEPOT;
    int page = 1;
    int pageSize = 4;
    List<TransactionTypeDto> transactionTypes = List.of(TransactionTypeDto.SELL, TransactionTypeDto.DIVIDEND);
    MvcResult mvcResult =
        getSecurities(depotId, page, pageSize, transactionTypes).andExpect(status().isOk()).andReturn();
    PaginatedTransactionReadDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PaginatedTransactionReadDto.class);

    assertThat(responseBody.getTotal()).isEqualTo(10);
    assertThat(responseBody.getCurrentPage()).isEqualTo(page);
    assertThat(responseBody.getLastPage()).isEqualTo(2);
    assertThat(responseBody.getPageSize()).isEqualTo(pageSize);
    verifyTransactionIds(List.of(11L, 14L, 16L, 19L), responseBody.getItems());

    TransactionReadDto transaction = new TransactionReadDto();
    transaction.setId(14L);
    transaction.setDate(LocalDate.of(2021, Month.JULY, 5));
    transaction.setTime(null);
    transaction.setSecurityId(SecurityIds.NVDA);
    transaction.setTransactionType(TransactionTypeDto.DIVIDEND);
    transaction.setSecurityCountOriginal(2.16);
    transaction.setSecurityCountSplitAdjusted(8.64);
    transaction.setGrossValue(0.3);
    transaction.setTax(0.07);
    transaction.setFee(null);
    transaction.setNetValue(0.23);
    transaction.setVersion(1L);
    assertThat(responseBody.getItems().get(1)).isEqualTo(transaction);
  }

  @Test
  void getFirstDepot_FirstPage_reverseOrder_ok() throws Exception {
    long depotId = DepotIds.FIRST_DEPOT;
    MvcResult mvcResult = mockMvc.perform(get(String.format(ENDPOINT, depotId)).queryParam("orderByTime", "DESC"))
        .andExpect(status().isOk()).andReturn();
    PaginatedTransactionReadDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PaginatedTransactionReadDto.class);

    assertThat(responseBody.getTotal()).isEqualTo(21);
    assertThat(responseBody.getCurrentPage()).isZero();
    assertThat(responseBody.getLastPage()).isEqualTo(2);
    assertThat(responseBody.getPageSize()).isEqualTo(10);
    verifyTransactionIds(List.of(32L, 31L, 26L, 25L, 24L, 39L, 19L, 16L, 15L, 14L), responseBody.getItems());
  }

  @Test
  void getFirstDepot_FirstPage_minDate_ok() throws Exception {
    long depotId = DepotIds.FIRST_DEPOT;
    MvcResult mvcResult = mockMvc.perform(get(String.format(ENDPOINT, depotId)).queryParam("minDate", "2023-08-01"))
        .andExpect(status().isOk()).andReturn();
    PaginatedTransactionReadDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PaginatedTransactionReadDto.class);

    assertThat(responseBody.getTotal()).isEqualTo(5);
    assertThat(responseBody.getCurrentPage()).isZero();
    assertThat(responseBody.getLastPage()).isZero();
    assertThat(responseBody.getPageSize()).isEqualTo(10);
    verifyTransactionIds(List.of(24L, 25L, 26L, 31L, 32L), responseBody.getItems());
  }

  @Test
  void getFirstDepot_FirstPage_maxDate_ok() throws Exception {
    long depotId = DepotIds.FIRST_DEPOT;
    MvcResult mvcResult = mockMvc.perform(get(String.format(ENDPOINT, depotId)).queryParam("maxDate", "2021-01-01"))
        .andExpect(status().isOk()).andReturn();
    PaginatedTransactionReadDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PaginatedTransactionReadDto.class);

    assertThat(responseBody.getTotal()).isEqualTo(8);
    assertThat(responseBody.getCurrentPage()).isZero();
    assertThat(responseBody.getLastPage()).isZero();
    assertThat(responseBody.getPageSize()).isEqualTo(10);
    verifyTransactionIds(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L), responseBody.getItems());
  }

  @Test
  void getFirstDepot_FirstPage_filterSecurityIds_ok() throws Exception {
    long depotId = DepotIds.FIRST_DEPOT;
    MvcResult mvcResult = mockMvc.perform(
        get(String.format(ENDPOINT, depotId)).queryParam("securityIds", Long.toString(SecurityIds.AMZN),
            Long.toString(SecurityIds.NVDA))).andExpect(status().isOk()).andReturn();
    PaginatedTransactionReadDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PaginatedTransactionReadDto.class);

    assertThat(responseBody.getTotal()).isEqualTo(15);
    assertThat(responseBody.getCurrentPage()).isZero();
    assertThat(responseBody.getLastPage()).isOne();
    assertThat(responseBody.getPageSize()).isEqualTo(10);
    verifyTransactionIds(List.of(1L, 2L, 3L, 4L, 5L, 6L, 8L, 10L, 12L, 14L), responseBody.getItems());
  }

  @Test
  void getFirstDepot_allFilters_desc_ok() throws Exception {
    long depotId = DepotIds.FIRST_DEPOT;
    MvcResult mvcResult = mockMvc.perform(
            get(String.format(ENDPOINT, depotId)).queryParam("orderByTime", "DESC").queryParam("minDate", "2023-01-01")
                .queryParam("maxDate", "2023-12-12")
                .queryParam("securityIds", Long.toString(SecurityIds.LVMH), Long.toString(SecurityIds.HAG))
                .queryParam("transactionTypes", TransactionTypeDto.DIVIDEND.name(), TransactionTypeDto.BUY.name()))
        .andExpect(status().isOk()).andReturn();
    PaginatedTransactionReadDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PaginatedTransactionReadDto.class);

    assertThat(responseBody.getTotal()).isEqualTo(3);
    assertThat(responseBody.getCurrentPage()).isZero();
    assertThat(responseBody.getLastPage()).isZero();
    assertThat(responseBody.getPageSize()).isEqualTo(10);
    verifyTransactionIds(List.of(26L, 25L, 24L), responseBody.getItems());
  }

  @Test
  void getFirstDepot_taxTransactions_noContent() throws Exception {
    long depotId = DepotIds.FIRST_DEPOT;
    List<TransactionTypeDto> transactionTypes = List.of(TransactionTypeDto.TAX);
    MvcResult mvcResult =
        getSecurities(depotId, null, null, transactionTypes).andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void getFirstDepot_emptyTransactionTypes_badRequest() throws Exception {
    long depotId = DepotIds.FIRST_DEPOT;
    List<TransactionTypeDto> transactionTypes = List.of();
    MvcResult mvcResult =
        getSecurities(depotId, null, null, transactionTypes).andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void getFirstDepot_emptySecurityIds_badRequest() throws Exception {
    long depotId = DepotIds.FIRST_DEPOT;
    MvcResult mvcResult = mockMvc.perform(get(String.format(ENDPOINT, depotId)).queryParam("securityIds", ""))
        .andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void getFirstDepot_minDateBeforeMaxDate_badRequest() throws Exception {
    long depotId = DepotIds.FIRST_DEPOT;
    MvcResult mvcResult = mockMvc.perform(
            get(String.format(ENDPOINT, depotId)).queryParam("minDate", "2023-01-01").queryParam("maxDate", "2022-01-01"))
        .andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void pageSizeIsZero_badRequest() throws Exception {
    MvcResult mvcResult = getSecurities(DepotIds.FIRST_DEPOT, null, 0).andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void pageSizeIsNegative_badRequest() throws Exception {
    MvcResult mvcResult = getSecurities(DepotIds.FIRST_DEPOT, null, -1).andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void pageIsNegative_badRequest() throws Exception {
    MvcResult mvcResult = getSecurities(DepotIds.FIRST_DEPOT, -1, null).andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void pageAndPageSizeAreNegative_badRequest() throws Exception {
    MvcResult mvcResult = getSecurities(DepotIds.FIRST_DEPOT, -1, -1).andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void depotDoesNotExist_noContent() throws Exception {
    MvcResult mvcResult = getSecurities(999, null, null).andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  private ResultActions getSecurities(long depotId, Integer page, Integer pageSize) throws Exception {
    MockHttpServletRequestBuilder requestBuilder = get(String.format(ENDPOINT, depotId));
    if (page != null) {
      requestBuilder = requestBuilder.param("page", page.toString());
    }
    if (pageSize != null) {
      requestBuilder = requestBuilder.param("pageSize", pageSize.toString());
    }
    return mockMvc.perform(requestBuilder);
  }

  private ResultActions getSecurities(long depotId, Integer page, Integer pageSize,
                                      List<TransactionTypeDto> transactionTypes) throws Exception {
    MockHttpServletRequestBuilder requestBuilder = get(String.format(ENDPOINT, depotId));
    if (page != null) {
      requestBuilder = requestBuilder.param("page", page.toString());
    }
    if (pageSize != null) {
      requestBuilder = requestBuilder.param("pageSize", pageSize.toString());
    }
    if (transactionTypes.isEmpty()) {
      requestBuilder = requestBuilder.param("transactionTypes", "");
    } else {
      requestBuilder = requestBuilder.param("transactionTypes",
          transactionTypes.stream().map(TransactionTypeDto::name).toArray(String[]::new));
    }
    return mockMvc.perform(requestBuilder);
  }

  private void verifyTransactionIds(List<Long> expectedTransactionIds, List<TransactionReadDto> transactions) {
    assertThat(transactions).hasSize(expectedTransactionIds.size());
    for (int i = 0; i < expectedTransactionIds.size(); i++) {
      assertThat(transactions.get(i).getId()).isEqualTo(expectedTransactionIds.get(i));
    }
  }
}
