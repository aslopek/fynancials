package de.as.fynancials.price.security.historical;


import static integration.MockServerUtils.respondWithFixture;
import static java.time.Month.DECEMBER;
import static java.time.Month.OCTOBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.as.fynancials.exchangerates.CurrencyConversionRequest;
import de.as.fynancials.exchangerates.ExchangeRateService;
import de.as.fynancials.price.security.historical.api.model.HistoricalSecurityPriceConfigDto;
import integration.IntegrationTest;
import integration.SecurityIds;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.client.RestTemplate;

@IntegrationTest
class UpdateHistoricalSecurityPriceConfigTest {

  private static final String ENDPOINT = "/securities/%d/historicalprices/config";

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @MockitoBean
  private ExchangeRateService exchangeRateServiceMock;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private HistoricalSecurityPriceRepository priceRepository;

  @Autowired
  private HistoricalSecurityPriceConfigRepository configRepository;

  @MockitoBean
  private Clock clock;

  private MockRestServiceServer mockServer;

  /**
   * Initialized with values for AMZN stock.
   */
  private HistoricalSecurityPriceConfigDto amznDataSource101;

  /**
   * Initialized with values for QCOM stock.
   */
  private HistoricalSecurityPriceConfigDto qcomDataSource102;

  @BeforeEach
  void beforeEach() {
    mockServer = MockRestServiceServer.createServer(restTemplate);
    when(clock.instant()).thenReturn(Instant.parse("2024-01-01T16:37:08Z"));
    when(clock.getZone()).thenReturn(ZoneId.of("Europe/Berlin"));

    when(exchangeRateServiceMock.convert(anyList(), any(), any())).thenAnswer(invocation -> {
      List<CurrencyConversionRequest> items = invocation.getArgument(0);
      return items.stream().map(CurrencyConversionRequest::getValue).toList();
    });

    amznDataSource101 = new HistoricalSecurityPriceConfigDto();
    amznDataSource101.setDataSourceId(101L);
    amznDataSource101.setExternalSecurityId("AMZ.DE");
    amznDataSource101.setIsActive(true);
    amznDataSource101.setVersion(0L);

    qcomDataSource102 = new HistoricalSecurityPriceConfigDto();
    qcomDataSource102.setDataSourceId(102L);
    qcomDataSource102.setExternalSecurityId("QCOM.XNAS");
    qcomDataSource102.setIsActive(true);
    qcomDataSource102.setVersion(0L);
  }

  @Test
  void changeDatasource_keepExistingPrices_ok() throws Exception {
    MvcResult mvcResult = putConfig(SecurityIds.AMZN, amznDataSource101, null).andExpect(status().isOk()).andReturn();
    HistoricalSecurityPriceConfigDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), HistoricalSecurityPriceConfigDto.class);
    amznDataSource101.setVersion(1L);
    assertThat(responseBody).isEqualTo(amznDataSource101);

    // verify database: config
    HistoricalSecurityPriceConfigEntity configEntity =
        configRepository.findBySecurityId(SecurityIds.AMZN).orElseThrow();
    assertThat(configEntity.getVersion()).isOne();
    assertThat(configEntity.getExternalSecurityId()).isEqualTo("AMZ.DE");
    assertThat(configEntity.isActive()).isTrue();

    // verify database: prices (first, last and date sanity check)
    List<HistoricalSecurityPriceEntity> prices =
        priceRepository.findAllBySecurityIdAndDateGreaterThanEqualOrderByDateAsc(SecurityIds.AMZN, LocalDate.EPOCH);
    assertThat(prices.getFirst().getPrice()).isEqualTo(new BigDecimal("134.74"));
    assertThat(prices.getFirst().getDate()).isEqualTo(LocalDate.of(2023, DECEMBER, 1));
    assertThat(prices.getFirst().getCurrency()).isEqualTo("EUR");
    assertThat(prices.getLast().getPrice()).isEqualTo(new BigDecimal("138.58"));
    assertThat(prices.getLast().getDate()).isEqualTo(LocalDate.of(2023, DECEMBER, 29));
    assertThat(prices.getLast().getCurrency()).isEqualTo("EUR");
    dateSanityCheck(prices);
  }

  @Test
  void changeDatasource_removeExistingPrices_ok() throws Exception {
    respondWithFixture(mockServer, "https://stock-price.api/v1/AMZ.DE?range=30d",
        "fixtures/historical-security-prices/data-source-101/amzn-30-days.json");
    respondWithFixture(mockServer, "https://stock-price.api/v1/AMZ.DE?range=1y",
        "fixtures/historical-security-prices/data-source-101/amzn-365-days.json");

    MvcResult mvcResult = putConfig(SecurityIds.AMZN, amznDataSource101, true).andExpect(status().isOk()).andReturn();
    HistoricalSecurityPriceConfigDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), HistoricalSecurityPriceConfigDto.class);
    amznDataSource101.setVersion(1L);
    assertThat(responseBody).isEqualTo(amznDataSource101);

    // verify database: config
    HistoricalSecurityPriceConfigEntity configEntity =
        configRepository.findBySecurityId(SecurityIds.AMZN).orElseThrow();
    assertThat(configEntity.getVersion()).isOne();
    assertThat(configEntity.getExternalSecurityId()).isEqualTo("AMZ.DE");
    assertThat(configEntity.isActive()).isTrue();

    // verify database: prices (first, last and date sanity check)
    List<HistoricalSecurityPriceEntity> prices =
        priceRepository.findAllBySecurityIdAndDateGreaterThanEqualOrderByDateAsc(SecurityIds.AMZN, LocalDate.EPOCH);
    assertThat(prices.getFirst().getPrice()).isEqualTo(new BigDecimal("78.6"));
    assertThat(prices.getFirst().getDate()).isEqualTo(LocalDate.of(2022, DECEMBER, 29));
    assertThat(prices.getFirst().getCurrency()).isEqualTo("EUR");
    assertThat(prices.getLast().getPrice()).isEqualTo(new BigDecimal("138.58"));
    assertThat(prices.getLast().getDate()).isEqualTo(LocalDate.of(2023, DECEMBER, 29));
    assertThat(prices.getLast().getCurrency()).isEqualTo("EUR");
    dateSanityCheck(prices);
  }

  @Test
  void addNewConfiguration_fetchPrices_ok() throws Exception {
    respondWithFixture(mockServer,
        "https://stock-price.api/v2?isin=QCOM.XNAS",
        "fixtures/historical-security-prices/data-source-102/qcom-73-days.json");
    MvcResult mvcResult = putConfig(SecurityIds.QCOM, qcomDataSource102, null).andExpect(status().isOk()).andReturn();
    HistoricalSecurityPriceConfigDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), HistoricalSecurityPriceConfigDto.class);
    assertThat(responseBody.getExternalSecurityId()).isEqualTo("QCOM.XNAS");
    assertThat(responseBody.getIsActive()).isTrue();
    assertThat(responseBody.getVersion()).isZero();

    // verify database: config
    HistoricalSecurityPriceConfigEntity configEntity =
        configRepository.findBySecurityId(SecurityIds.QCOM).orElseThrow();
    assertThat(configEntity.getExternalSecurityId()).isEqualTo(responseBody.getExternalSecurityId());
    assertThat(configEntity.isActive()).isEqualTo(responseBody.getIsActive());
    assertThat(configEntity.getVersion()).isEqualTo(responseBody.getVersion());

    // verify database: prices (first, last and date sanity check)
    List<HistoricalSecurityPriceEntity> prices =
        priceRepository.findAllBySecurityIdAndDateGreaterThanEqualOrderByDateAsc(SecurityIds.QCOM, LocalDate.EPOCH);
    assertThat(prices).hasSize(52);
    assertThat(prices.getFirst().getPrice()).isEqualTo(new BigDecimal("104.62"));
    assertThat(prices.getFirst().getDate()).isEqualTo(LocalDate.of(2023, OCTOBER, 17));
    assertThat(prices.getFirst().getCurrency()).isEqualTo("USD");
    assertThat(prices.getLast().getPrice()).isEqualTo(new BigDecimal("131.96"));
    assertThat(prices.getLast().getDate()).isEqualTo(LocalDate.of(2023, DECEMBER, 29));
    assertThat(prices.getLast().getCurrency()).isEqualTo("USD");
    dateSanityCheck(prices);
  }

  @Test
  void addNewConfiguration_inactive_ok() throws Exception {
    qcomDataSource102.setIsActive(false);
    MvcResult mvcResult = putConfig(SecurityIds.QCOM, qcomDataSource102, null).andExpect(status().isOk()).andReturn();
    HistoricalSecurityPriceConfigDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), HistoricalSecurityPriceConfigDto.class);
    assertThat(responseBody.getExternalSecurityId()).isEqualTo("QCOM.XNAS");
    assertThat(responseBody.getIsActive()).isFalse();
    assertThat(responseBody.getVersion()).isZero();

    // verify database: config
    HistoricalSecurityPriceConfigEntity configEntity =
        configRepository.findBySecurityId(SecurityIds.QCOM).orElseThrow();
    assertThat(configEntity.getExternalSecurityId()).isEqualTo(responseBody.getExternalSecurityId());
    assertThat(configEntity.isActive()).isEqualTo(responseBody.getIsActive());
    assertThat(configEntity.getVersion()).isEqualTo(responseBody.getVersion());

    // verify database: prices
    List<HistoricalSecurityPriceEntity> prices =
        priceRepository.findAllBySecurityIdAndDateGreaterThanEqualOrderByDateAsc(SecurityIds.QCOM, LocalDate.EPOCH);
    assertThat(prices).isEmpty();
  }

  @Test
  void fetchPrices_ignoreNulls_ok() throws Exception {
    when(clock.instant()).thenReturn(Instant.parse("2024-01-26T20:44:38Z"));
    when(clock.getZone()).thenReturn(ZoneId.of("Europe/Berlin"));

    respondWithFixture(mockServer, "https://stock-price.api/v3/stocks/prices?id=VNGGF&range=MAX",
        "fixtures/historical-security-prices/data-source-103/vnggf-5-days.json");

    HistoricalSecurityPriceConfigDto config = new HistoricalSecurityPriceConfigDto();
    config.setExternalSecurityId("VNGGF");
    config.setDataSourceId(103L);
    config.setIsActive(true);
    config.setVersion(0L);

    MvcResult mvcResult = putConfig(SecurityIds.VNGGF, config, null).andExpect(status().isOk()).andReturn();
    HistoricalSecurityPriceConfigDto responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), HistoricalSecurityPriceConfigDto.class);
    assertThat(responseBody.getExternalSecurityId()).isEqualTo("VNGGF");
    assertThat(responseBody.getIsActive()).isTrue();
    assertThat(responseBody.getVersion()).isZero();

    // verify database: config
    HistoricalSecurityPriceConfigEntity configEntity =
        configRepository.findBySecurityId(SecurityIds.VNGGF).orElseThrow();
    assertThat(configEntity.getExternalSecurityId()).isEqualTo(responseBody.getExternalSecurityId());
    assertThat(configEntity.isActive()).isEqualTo(responseBody.getIsActive());
    assertThat(configEntity.getVersion()).isEqualTo(responseBody.getVersion());

    // verify database: prices
    List<HistoricalSecurityPriceEntity> prices =
        priceRepository.findAllBySecurityIdAndDateGreaterThanEqualOrderByDateAsc(SecurityIds.VNGGF, LocalDate.EPOCH);
    assertThat(prices).hasSize(2);

    HistoricalSecurityPriceEntity price = prices.getFirst();
    assertThat(price.getSecurityId()).isEqualTo(SecurityIds.VNGGF);
    assertThat(price.getPrice()).isEqualByComparingTo(new BigDecimal("56.469"));
    assertThat(price.getCurrency()).isEqualTo("EUR");
    assertThat(price.getDate()).isEqualTo(LocalDate.of(2024, Month.JANUARY, 19));

    price = prices.get(1);
    assertThat(price.getSecurityId()).isEqualTo(SecurityIds.VNGGF);
    assertThat(price.getPrice()).isEqualByComparingTo(new BigDecimal("57.441"));
    assertThat(price.getCurrency()).isEqualTo("EUR");
    assertThat(price.getDate()).isEqualTo(LocalDate.of(2024, Month.JANUARY, 25));
  }

  @Test
  void updateDatasource_conflict() throws Exception {
    HistoricalSecurityPriceConfigDto requestBody = new HistoricalSecurityPriceConfigDto();
    requestBody.setExternalSecurityId("CRM");
    requestBody.setDataSourceId(103L);
    requestBody.setIsActive(true);
    requestBody.setVersion(0L);

    MvcResult mvcResult = putConfig(SecurityIds.CRM, requestBody, null).andExpect(status().isConflict()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();

    // verify database: config (no changes)
    HistoricalSecurityPriceConfigEntity configEntity = configRepository.findBySecurityId(SecurityIds.CRM).orElseThrow();
    assertThat(configEntity.getVersion()).isOne();
    assertThat(configEntity.isActive()).isFalse();
  }

  @Test
  void updateDatasource_securityDoesNotExist_badRequest() throws Exception {
    long configCount = configRepository.count();
    MvcResult mvcResult = putConfig(999, amznDataSource101, null).andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(configRepository.count()).isEqualTo(configCount);
  }

  private ResultActions putConfig(long securityId, HistoricalSecurityPriceConfigDto config,
                                  Boolean removeExistingPrices) throws Exception {
    String requestBody = objectMapper.writeValueAsString(config);
    String url = String.format(ENDPOINT, securityId);
    MockHttpServletRequestBuilder requestBuilder = put(url);
    if (removeExistingPrices != null) {
      requestBuilder = requestBuilder.queryParam("removeExistingPrices", Boolean.toString(removeExistingPrices));
    }
    return mockMvc.perform(requestBuilder.contentType(MediaType.APPLICATION_JSON).content(requestBody));
  }

  private void dateSanityCheck(List<HistoricalSecurityPriceEntity> prices) {
    for (int i = 1; i < prices.size(); i++) {
      assertThat(prices.get(i - 1).getDate()).isBefore(prices.get(i).getDate());
    }
  }
}
