package de.as.fynancials.price.security.historical;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.SqlMergeMode.MergeMode.MERGE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.as.fynancials.price.security.historical.api.model.HistoricalSecurityPriceDto;
import integration.IntegrationTest;
import integration.SecurityIds;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@IntegrationTest
class GetHistoricalSecurityPricesTest {

  private static final String ENDPOINT = "/securities/%d/historicalprices";
  private static final Offset<Double> ACCURACY_ONE_THOUSANDTH = Offset.strictOffset(0.001);

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Autowired
  private MockMvc mockMvc;

  @Test
  void getAllPrices_ok() throws Exception {
    MvcResult mvcResult =
        mockMvc.perform(get(String.format(ENDPOINT, SecurityIds.PINS))).andExpect(status().isOk()).andReturn();
    List<HistoricalSecurityPriceDto> prices =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});
    assertThat(prices).hasSize(20);

    // verify date range and sorting by date
    assertThat(prices.getFirst().getDate()).isEqualTo(LocalDate.of(2023, Month.DECEMBER, 1));
    assertThat(prices.get(19).getDate()).isEqualTo(LocalDate.of(2023, Month.DECEMBER, 29));
    for (int i = 1; i < prices.size(); i++) {
      assertThat(prices.get(i).getDate()).isAfter(prices.get(i - 1).getDate());
    }

    // verify 'common' properties
    for (HistoricalSecurityPriceDto price : prices) {
      assertThat(price.getSecurityId()).isEqualTo(SecurityIds.PINS);
      assertThat(price.getCurrency()).isEqualTo("USD");
    }

    // verify values
    List<Double> priceValues = prices.stream().map(HistoricalSecurityPriceDto::getPrice).toList();
    assertThat(priceValues).containsExactlyElementsOf(
        List.of(34.79, 34.5, 34.11, 33.52, 34.02, 34.91, 35.36, 36.13, 36.51, 37.01, 37.37, 37.7, 38.04, 37.12, 37.36,
            37.38, 37.16, 37.3, 37.27, 37.04));
  }

  @Test
  void getAllPrices_startDate_ok() throws Exception {
    MvcResult mvcResult =
        mockMvc.perform(get(String.format(ENDPOINT, SecurityIds.AMZN)).queryParam("startDate", "2023-12-21"))
            .andExpect(status().isOk()).andReturn();
    List<HistoricalSecurityPriceDto> prices =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});
    assertThat(prices).hasSize(5);

    // verify date range and sorting by date
    assertThat(prices.get(0).getDate()).isEqualTo(LocalDate.of(2023, Month.DECEMBER, 21));
    assertThat(prices.get(4).getDate()).isEqualTo(LocalDate.of(2023, Month.DECEMBER, 29));
    for (int i = 1; i < prices.size(); i++) {
      assertThat(prices.get(i).getDate()).isAfter(prices.get(i - 1).getDate());
    }

    // verify 'common' properties
    for (HistoricalSecurityPriceDto price : prices) {
      assertThat(price.getSecurityId()).isEqualTo(SecurityIds.AMZN);
      assertThat(price.getCurrency()).isEqualTo("EUR");
    }

    // verify values
    List<Double> priceValues = prices.stream().map(HistoricalSecurityPriceDto::getPrice).toList();
    assertThat(priceValues).containsExactlyElementsOf(List.of(139.2, 139.26, 138.22, 138.34, 138.58));
  }

  @Test
  @SqlMergeMode(MERGE)
  @Sql(statements = "DELETE FROM HISTORICAL_SECURITY_PRICE WHERE DATE > '2023-12-08'")
  void getAllPrices_currencyEurToUsd_ok() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get(String.format(ENDPOINT, SecurityIds.AMZN)).queryParam("currency", "USD"))
        .andExpect(status().isOk()).andReturn();
    List<HistoricalSecurityPriceDto> prices =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});
    assertThat(prices).hasSize(6);

    // verify date range and sorting by date
    assertThat(prices.get(0).getDate()).isEqualTo(LocalDate.of(2023, Month.DECEMBER, 1));
    assertThat(prices.get(5).getDate()).isEqualTo(LocalDate.of(2023, Month.DECEMBER, 8));
    for (int i = 1; i < prices.size(); i++) {
      assertThat(prices.get(i).getDate()).isAfter(prices.get(i - 1).getDate());
    }

    // verify 'common' properties
    for (HistoricalSecurityPriceDto price : prices) {
      assertThat(price.getSecurityId()).isEqualTo(SecurityIds.AMZN);
      assertThat(price.getCurrency()).isEqualTo("USD");
    }

    // verify values
    List<Double> priceValues = prices.stream().map(HistoricalSecurityPriceDto::getPrice).toList();
    assertThat(priceValues).zipSatisfy(List.of(146.52975, 144.89218, 147.1112, 145.1581, 146.78719, 147.10605),
        (actual, expected) -> assertThat(actual).isCloseTo(expected, ACCURACY_ONE_THOUSANDTH));
  }

  @Test
  @SqlMergeMode(MERGE)
  @Sql(statements = "DELETE FROM HISTORICAL_SECURITY_PRICE WHERE DATE > '2023-12-08'")
  void getAllPrices_currencyUsdToEur_ok() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get(String.format(ENDPOINT, SecurityIds.PINS)).queryParam("currency", "EUR"))
        .andExpect(status().isOk()).andReturn();
    List<HistoricalSecurityPriceDto> prices =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});
    assertThat(prices).hasSize(6);

    // verify date range and sorting by date
    assertThat(prices.get(0).getDate()).isEqualTo(LocalDate.of(2023, Month.DECEMBER, 1));
    assertThat(prices.get(5).getDate()).isEqualTo(LocalDate.of(2023, Month.DECEMBER, 8));
    for (int i = 1; i < prices.size(); i++) {
      assertThat(prices.get(i).getDate()).isAfter(prices.get(i - 1).getDate());
    }

    // verify 'common' properties
    for (HistoricalSecurityPriceDto price : prices) {
      assertThat(price.getSecurityId()).isEqualTo(SecurityIds.PINS);
      assertThat(price.getCurrency()).isEqualTo("EUR");
    }

    // verify values
    List<Double> priceValues = prices.stream().map(HistoricalSecurityPriceDto::getPrice).toList();
    assertThat(priceValues).zipSatisfy(List.of(31.9908, 31.74457, 31.5337, 31.10039, 31.58481, 32.39306),
        (actual, expected) -> assertThat(actual).isCloseTo(expected, ACCURACY_ONE_THOUSANDTH));
  }

  @Test
  @SqlMergeMode(MERGE)
  @Sql(statements = "DELETE FROM HISTORICAL_SECURITY_PRICE WHERE DATE > '2023-12-08'")
  void getAllPrices_currencyUsdToEur_startDate_ok() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get(String.format(ENDPOINT, SecurityIds.PINS)).queryParam("currency", "EUR")
        .queryParam("startDate", "2023-12-08")).andExpect(status().isOk()).andReturn();
    List<HistoricalSecurityPriceDto> prices =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});
    assertThat(prices).hasSize(1);
    HistoricalSecurityPriceDto price = prices.getFirst();
    assertThat(price.getSecurityId()).isEqualTo(SecurityIds.PINS);
    assertThat(price.getPrice()).isCloseTo(32.39306, ACCURACY_ONE_THOUSANDTH);
    assertThat(price.getCurrency()).isEqualTo("EUR");
    assertThat(price.getDate()).isEqualTo(LocalDate.of(2023, Month.DECEMBER, 8));
  }

  @Test
  void getAllPrices_securityDoesNotExist() throws Exception {
    MvcResult mvcResult =
        mockMvc.perform(get(String.format(ENDPOINT, 999))).andExpect(status().isNotFound()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void getAllPrices_securityHasNoPrices() throws Exception {
    MvcResult mvcResult =
        mockMvc.perform(get(String.format(ENDPOINT, SecurityIds.CRM))).andExpect(status().isNotFound()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void getAllPrices_currencyUnknown() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get(String.format(ENDPOINT, SecurityIds.PINS)).queryParam("currency", "ABC"))
        .andExpect(status().isNotFound()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void getAllPrices_currencyIsMalformed() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get(String.format(ENDPOINT, SecurityIds.PINS)).queryParam("currency", "US$"))
        .andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void getAllPrices_startDateIsMalformed() throws Exception {
    MvcResult mvcResult =
        mockMvc.perform(get(String.format(ENDPOINT, SecurityIds.PINS)).queryParam("startDate", "01.12.2021"))
            .andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }
}