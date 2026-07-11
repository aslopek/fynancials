package de.as.fynancials.cors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.as.fynancials.config.securitygroup.api.model.SecurityGroupCreateDto;
import de.as.fynancials.config.securitygroup.api.model.SecurityGroupUpdateDto;
import de.as.fynancials.depot.api.model.DepotCreateDto;
import de.as.fynancials.depot.api.model.DepotUpdateDto;
import de.as.fynancials.depot.transaction.api.model.TransactionCreateDto;
import de.as.fynancials.depot.transaction.api.model.TransactionUpdateDto;
import de.as.fynancials.notification.dividendannouncement.api.model.DividendAnnouncementConfigCreateDto;
import de.as.fynancials.notification.dividendannouncement.api.model.DividendAnnouncementConfigUpdateDto;
import de.as.fynancials.notification.dividendannouncement.api.model.DividendAnnouncementDataSourceCreateDto;
import de.as.fynancials.notification.dividendannouncement.api.model.DividendAnnouncementDataSourceUpdateDto;
import de.as.fynancials.notification.dividendannouncement.api.model.DividendAnnouncementUpdateDto;
import de.as.fynancials.price.security.historical.HistoricalSecurityPriceConfig;
import de.as.fynancials.price.security.historical.api.model.HistoricalSecurityPriceDataSourceCreateDto;
import de.as.fynancials.price.security.historical.api.model.HistoricalSecurityPriceDataSourceUpdateDto;
import de.as.fynancials.security.api.model.SecurityCreateDto;
import de.as.fynancials.security.api.model.SecurityUpdateDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@DirtiesContext
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
class CorsIntegrationTest {

  private static final String ALLOWED_ORIGIN_1 = "http://localhost:1234";
  private static final String ALLOWED_ORIGIN_2 = "https://localhost:1235";
  private static final String CORS_HEADER = "Access-Control-Allow-Origin";

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Autowired
  private MockMvc mockMvc;

  // /config/clients/{clientId}

  @Test
  void getClientConfig() throws Exception {
    testEndpoint(HttpMethod.GET, "/config/clients/some-client", null);
  }

  @Test
  void deleteClientConfig() throws Exception {
    testEndpoint(HttpMethod.DELETE, "/config/clients/some-client", null);
  }

  // /config/clients/{clientId}/{clientConfigKey}

  @Test
  void getClientConfigValue() throws Exception {
    testEndpoint(HttpMethod.GET, "/config/clients/some-client/some-key", null);
  }

  @Test
  void setClientConfigValue() throws Exception {
    testEndpoint(HttpMethod.PUT, "/config/clients/some-client/some-key", "some-value", MediaType.TEXT_PLAIN);
  }

  @Test
  void deleteClientConfigValue() throws Exception {
    testEndpoint(HttpMethod.DELETE, "/config/clients/some-client/some-key", null);
  }

  // /config/currency/default-currency

  @Test
  void getDefaultCurrency() throws Exception {
    testEndpoint(HttpMethod.GET, "/config/currency/default-currency", null);
  }

  // /config/currency/supported-currencies

  @Test
  void getSupportedCurrencies() throws Exception {
    testEndpoint(HttpMethod.GET, "/config/currency/supported-currencies", null);
  }

  // /config/database

  @Test
  void getDatabaseConfig() throws Exception {
    testEndpoint(HttpMethod.GET, "/config/database", null);
  }

  // /config/backend-services

  @Test
  void getBackendServicesInfo() throws Exception {
    testEndpoint(HttpMethod.GET, "/config/backend-services", null);
  }

  // /config/dev-mode

  @Test
  void isDevModeActive() throws Exception {
    testEndpoint(HttpMethod.GET, "/config/dev-mode", null);
  }

  @Test
  void setDevModeActive() throws Exception {
    testEndpoint(HttpMethod.PUT, "/config/dev-mode", true, MediaType.TEXT_PLAIN);
  }

  // /config/pid

  @Test
  void getPid() throws Exception {
    testEndpoint(HttpMethod.GET, "/config/pid", null);
  }

  // /config/security-groups

  @Test
  void createDepotSecurityGroup() throws Exception {
    testEndpoint(HttpMethod.POST, "/config/security-groups", new SecurityGroupCreateDto());
  }

  // /config/security-groups/{groupId}

  @Test
  void updateDepotSecurityGroup() throws Exception {
    testEndpoint(HttpMethod.POST, "/config/security-groups/1", new SecurityGroupUpdateDto());
  }

  @Test
  void deleteDepotSecurityGroup() throws Exception {
    testEndpoint(HttpMethod.DELETE, "/config/security-groups/1", null);
  }

  // /config/third-party-licenses

  @Test
  void getThirdPartyLicenses() throws Exception {
    testEndpoint(HttpMethod.GET, "/config/third-party-licenses", null);
  }

  // /depot-performance/income

  @Test
  void getIncome() throws Exception {
    testEndpoint(HttpMethod.GET, "/depot-performance/income", null);
  }

  // /depot-positions

  @Test
  void getDepotPositions() throws Exception {
    testEndpoint(HttpMethod.GET, "/depot-positions?depots=1", null);
  }

  //  /depots

  @Test
  void createDepot() throws Exception {
    testEndpoint(HttpMethod.POST, "/depots", new DepotCreateDto());
  }

  @Test
  void getDepots() throws Exception {
    testEndpoint(HttpMethod.GET, "/depots", null);
  }

  // /depots/{id}

  @Test
  void getDepot() throws Exception {
    testEndpoint(HttpMethod.GET, "/depots/1", null);
  }

  @Test
  void updateDepot() throws Exception {
    testEndpoint(HttpMethod.PUT, "/depots/1", new DepotUpdateDto());
  }

  @Test
  void deleteDepot() throws Exception {
    testEndpoint(HttpMethod.DELETE, "/depots/1", null);
  }

  // /depots/{depotId}/logo

  @Test
  void getLogo_depot() throws Exception {
    testEndpoint(HttpMethod.GET, "/depots/1/logo", null);
  }

  @Test
  void setLogo_depot() throws Exception {
    testEndpoint(HttpMethod.PUT, "/depots/1/logo", new byte[1]);
  }

  @Test
  void deleteLogo_depot() throws Exception {
    testEndpoint(HttpMethod.DELETE, "/depots/1/logo", null);
  }

  // /depots/{depotId}/securities/{securityId}/lots

  @Test
  void getLots() throws Exception {
    testEndpoint(HttpMethod.GET, "/depots/1/securities/1/lots", null);
  }

  // /depots/{depotId}/transactions

  @Test
  void createTransaction() throws Exception {
    testEndpoint(HttpMethod.POST, "/depots/1/transactions", new TransactionCreateDto());
  }

  // /depots/{depotId}/transactions/{transactionId}

  @Test
  void getTransaction() throws Exception {
    testEndpoint(HttpMethod.GET, "/depots/1/transactions/1", null);
  }

  @Test
  void updateTransaction() throws Exception {
    testEndpoint(HttpMethod.PUT, "/depots/1/transactions/1", new TransactionUpdateDto());
  }

  @Test
  void deleteTransaction() throws Exception {
    testEndpoint(HttpMethod.DELETE, "/depots/1/transactions/1", null);
  }

  // /dividends

  @Test
  void getDividends() throws Exception {
    testEndpoint(HttpMethod.GET, "/dividends?depots=[1]", null);
  }

  // /historicalprices/data-sources

  @Test
  void createHistoricalSecurityPriceDataSource() throws Exception {
    testEndpoint(HttpMethod.POST, "/historicalprices/data-sources", new HistoricalSecurityPriceDataSourceCreateDto());
  }

  @Test
  void getHistoricalSecurityPriceDataSources() throws Exception {
    testEndpoint(HttpMethod.GET, "/historicalprices/data-sources", null);
  }

  // /historicalprices/data-sources/{id}

  @Test
  void updateHistoricalSecurityPriceDataSource() throws Exception {
    testEndpoint(HttpMethod.PUT, "/historicalprices/data-sources/1", new HistoricalSecurityPriceDataSourceUpdateDto());
  }

  @Test
  void deleteHistoricalSecurityPriceDataSource() throws Exception {
    testEndpoint(HttpMethod.DELETE, "/historicalprices/data-sources/1", null);
  }

  // /notifications/dividend-announcements

  @Test
  void getDividendAnnouncements() throws Exception {
    testEndpoint(HttpMethod.GET, "/notifications/dividend-announcements", null);
  }

  // /notifications/dividend-announcements/{id}

  @Test
  void updateDividendAnnouncement() throws Exception {
    DividendAnnouncementUpdateDto requestBody = new DividendAnnouncementUpdateDto();
    testEndpoint(HttpMethod.PUT, "/notifications/dividend-announcements/1", requestBody);
  }

  // /notifications/dividend-announcements/configs

  @Test
  void getDividendAnnouncementConfigs() throws Exception {
    testEndpoint(HttpMethod.GET, "/notifications/dividend-announcements/configs", null);
  }

  // /notifications/dividend-announcements/configs/{securityId}

  @Test
  void getDividendAnnouncementConfig() throws Exception {
    testEndpoint(HttpMethod.GET, "/notifications/dividend-announcements/configs/1", null);
  }

  @Test
  void createDividendAnnouncementConfig() throws Exception {
    DividendAnnouncementConfigCreateDto requestBody = new DividendAnnouncementConfigCreateDto();
    testEndpoint(HttpMethod.POST, "/notifications/dividend-announcements/configs/1", requestBody);
  }

  @Test
  void updateDividendAnnouncementConfig() throws Exception {
    DividendAnnouncementConfigUpdateDto requestBody = new DividendAnnouncementConfigUpdateDto();
    testEndpoint(HttpMethod.PUT, "/notifications/dividend-announcements/configs/1", requestBody);
  }

  @Test
  void deleteDividendAnnouncementConfig() throws Exception {
    testEndpoint(HttpMethod.DELETE, "/notifications/dividend-announcements/configs/1", null);
  }

  // /notifications/dividend-announcements/data-sources

  @Test
  void createDividendAnnouncementDataSource() throws Exception {
    DividendAnnouncementDataSourceCreateDto requestBody = new DividendAnnouncementDataSourceCreateDto();
    testEndpoint(HttpMethod.POST, "/notifications/dividend-announcements/data-sources", requestBody);
  }

  @Test
  void getDividendAnnouncementDataSources() throws Exception {
    testEndpoint(HttpMethod.GET, "/notifications/dividend-announcements/data-sources", null);
  }

  // /notifications/dividend-announcements/data-sources/{id}

  @Test
  void updateDividendAnnouncementDataSource() throws Exception {
    DividendAnnouncementDataSourceUpdateDto requestBody = new DividendAnnouncementDataSourceUpdateDto();
    testEndpoint(HttpMethod.PUT, "/notifications/dividend-announcements/data-sources/1", requestBody);
  }

  @Test
  void deleteDividendAnnouncementDataSource() throws Exception {
    testEndpoint(HttpMethod.DELETE, "/notifications/dividend-announcements/data-sources/1", null);
  }

  // /search/securities

  @Test
  void searchSecurities() throws Exception {
    testEndpoint(HttpMethod.GET, "/search/securities", null);
  }

  // /securities

  @Test
  void createSecurity() throws Exception {
    SecurityCreateDto requestBody = new SecurityCreateDto();
    testEndpoint(HttpMethod.POST, "/securities", requestBody);
  }

  @Test
  void getSecurities() throws Exception {
    testEndpoint(HttpMethod.GET, "/securities", null);
  }

  // /securities/{id}

  @Test
  void deleteSecurity() throws Exception {
    testEndpoint(HttpMethod.DELETE, "/securities/10", null);
  }

  @Test
  void getSecurity() throws Exception {
    testEndpoint(HttpMethod.GET, "/securities/1", null);
  }

  @Test
  void updateSecurity() throws Exception {
    SecurityUpdateDto requestBody = new SecurityUpdateDto();
    testEndpoint(HttpMethod.PUT, "/securities/1", requestBody);
  }

  // /securities/{securityId}/cagr

  @Test
  void getCagr() throws Exception {
    testEndpoint(HttpMethod.GET, "/securities/1/cagr", null);
  }

  // /securities/{securityId}/historicalprices

  @Test
  void getHistoricalPrices() throws Exception {
    testEndpoint(HttpMethod.GET, "/securities/1/historicalprices", null);
  }

  // /securities/{securityId}/historicalprices/config

  @Test
  void getHistoricalPriceConfig() throws Exception {
    testEndpoint(HttpMethod.GET, "/securities/1/historicalprices/config", null);
  }

  @Test
  void setHistoricalPriceConfig() throws Exception {
    HistoricalSecurityPriceConfig requestBody = new HistoricalSecurityPriceConfig();
    testEndpoint(HttpMethod.GET, "/securities/1/historicalprices/config", requestBody);
  }

  // /securities/{securityId}/logo

  @Test
  void getLogo_security() throws Exception {
    testEndpoint(HttpMethod.GET, "/securities/1/logo", null);
  }

  @Test
  void setLogo_security() throws Exception {
    testEndpoint(HttpMethod.PUT, "/securities/1/logo", new byte[1]);
  }

  @Test
  void deleteLogo_security() throws Exception {
    testEndpoint(HttpMethod.DELETE, "/securities/1/logo", null);
  }

  private void testEndpoint(HttpMethod method, String url, Object requestBody) throws Exception {
    testEndpoint(method, url, requestBody, MediaType.APPLICATION_JSON);
  }

  private void testEndpoint(HttpMethod method, String url, Object requestBody, MediaType contentType) throws Exception {
    // request with first allowed origin
    MockHttpServletRequestBuilder builder = request(method, url).header("Origin", ALLOWED_ORIGIN_1);
    if (requestBody != null) {
      String content = objectMapper.writeValueAsString(requestBody);
      builder.content(content).contentType(contentType);
    }
    MvcResult result = mockMvc.perform(builder).andReturn();
    assertThat(result.getResponse().getHeader(CORS_HEADER)).isEqualTo(ALLOWED_ORIGIN_1);

    // request with second allowed origin
    builder = request(method, url).header("Origin", ALLOWED_ORIGIN_2);
    if (requestBody != null) {
      String content = objectMapper.writeValueAsString(requestBody);
      builder.content(content).contentType(contentType);
    }
    result = mockMvc.perform(builder).andReturn();
    assertThat(result.getResponse().getHeader(CORS_HEADER)).isEqualTo(ALLOWED_ORIGIN_2);

    // request with some not-allowed origin
    builder = request(method, url).header("Origin", "http://localhost:1236");
    if (requestBody != null) {
      String content = objectMapper.writeValueAsString(requestBody);
      builder.content(content).contentType(contentType);
    }
    result = mockMvc.perform(builder).andReturn();
    assertThat(result.getResponse().getHeaderNames()).doesNotContain(CORS_HEADER);
  }
}
