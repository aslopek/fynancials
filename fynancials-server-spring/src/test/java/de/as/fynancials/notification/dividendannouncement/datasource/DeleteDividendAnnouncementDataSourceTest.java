package de.as.fynancials.notification.dividendannouncement.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.SqlMergeMode.MergeMode.MERGE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import integration.IntegrationTest;
import integration.sql.TestDataQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class DeleteDividendAnnouncementDataSourceTest {

  private static final String ENDPOINT = "/notifications/dividend-announcements/data-sources/%d";

  @Autowired
  private DividendAnnouncementDataSourceRepository dataSourceRepository;

  @Autowired
  private MockMvc mockMvc;

  @Test
  @SqlMergeMode(MERGE)
  @Sql(statements = """
      DELETE FROM DIVIDEND_ANNOUNCEMENT_CONFIG;
      """)
  void deleteDataSource_noHeadersNoMappings_ok() throws Exception {
    long id = 101; // Existing source from test data with 0 headers, 0 mappings
    long dataSourceCount = dataSourceRepository.count();

    MvcResult mvcResult = deleteDataSource(id).andExpect(status().isNoContent()).andReturn();

    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(dataSourceRepository.count()).isEqualTo(dataSourceCount - 1);
    assertThat(dataSourceRepository.existsById(id)).isFalse();

    // Verify cascaded deletion of announcements
    assertThat(TestDataQuery.getDividendAnnouncementCountByDataSourceId(id)).isZero();
    assertThat(TestDataQuery.getDividendAnnouncementCountByDataSourceId(102)).isEqualTo(3);

    assertThat(TestDataQuery.getDividendAnnouncementDataSourceHeadersByDataSourceId(id)).isZero();
    assertThat(TestDataQuery.getDividendAnnouncementDataSourceHeadersByDataSourceId(102)).isEqualTo(2);
  }

  @Test
  @SqlMergeMode(MERGE)
  @Sql(statements = """
      DELETE FROM DIVIDEND_ANNOUNCEMENT_CONFIG;
      """)
  void deleteDataSource_withHeaders_ok() throws Exception {
    long id = 102; // Existing source from test data with 2 headers, 0 mappings
    long dataSourceCount = dataSourceRepository.count();

    MvcResult mvcResult = deleteDataSource(id).andExpect(status().isNoContent()).andReturn();

    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(dataSourceRepository.count()).isEqualTo(dataSourceCount - 1);
    assertThat(dataSourceRepository.existsById(id)).isFalse();

    assertThat(TestDataQuery.getDividendAnnouncementCountByDataSourceId(101)).isEqualTo(5);
    assertThat(TestDataQuery.getDividendAnnouncementCountByDataSourceId(id)).isZero();

    // Verify cascaded deletion of headers
    assertThat(TestDataQuery.getDividendAnnouncementDataSourceHeadersByDataSourceId(101)).isOne();
    assertThat(TestDataQuery.getDividendAnnouncementDataSourceHeadersByDataSourceId(id)).isZero();
  }

  @Test
  @SqlMergeMode(MERGE)
  @Sql(statements = """
      DELETE FROM DIVIDEND_ANNOUNCEMENT_CONFIG;
      """)
  void deleteDataSource_withCurrencyMappings_ok() throws Exception {
    long id = 103; // Existing source from test data with 0 headers, 3 mappings
    long dataSourceCount = dataSourceRepository.count();

    MvcResult mvcResult = deleteDataSource(id).andExpect(status().isNoContent()).andReturn();

    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(dataSourceRepository.count()).isEqualTo(dataSourceCount - 1);
    assertThat(dataSourceRepository.existsById(id)).isFalse();

    // Verify cascaded deletion of currency mappings
    assertThat(TestDataQuery.getDividendAnnouncementDataSourceCurrencyMappingsByDataSourceId(id)).isZero();
  }

  @Test
  void deleteDataSource_hasConfig_badRequest() throws Exception {
    long id = 101;
    long dataSourceCount = dataSourceRepository.count();

    MvcResult mvcResult = deleteDataSource(id).andExpect(status().isBadRequest()).andReturn();

    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(dataSourceRepository.count()).isEqualTo(dataSourceCount);
    assertThat(dataSourceRepository.existsById(id)).isTrue();
    assertThat(TestDataQuery.getDividendAnnouncementCountByDataSourceId(id)).isEqualTo(5);
    assertThat(TestDataQuery.getDividendAnnouncementConfigCountByDataSourceId(id)).isEqualTo(3);
    assertThat(TestDataQuery.getDividendAnnouncementDataSourceHeadersByDataSourceId(id)).isOne();
    assertThat(TestDataQuery.getDividendAnnouncementDataSourceCurrencyMappingsByDataSourceId(id)).isZero();
  }

  @Test
  void deleteDataSource_notFound() throws Exception {
    long id = 999;
    long dataSourceCount = dataSourceRepository.count();

    MvcResult mvcResult = deleteDataSource(id).andExpect(status().isNotFound()).andReturn();

    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(dataSourceRepository.count()).isEqualTo(dataSourceCount);

    // Verify that the existing entities and their collections were unchanged
    assertThat(TestDataQuery.getDividendAnnouncementCountByDataSourceId(101)).isEqualTo(5);
    assertThat(TestDataQuery.getDividendAnnouncementCountByDataSourceId(102)).isEqualTo(3);
    assertThat(TestDataQuery.getDividendAnnouncementConfigCountByDataSourceId(101)).isEqualTo(3);
    assertThat(TestDataQuery.getDividendAnnouncementConfigCountByDataSourceId(102)).isEqualTo(3);
    assertThat(TestDataQuery.getDividendAnnouncementDataSourceHeadersByDataSourceId(101)).isOne();
    assertThat(TestDataQuery.getDividendAnnouncementDataSourceHeadersByDataSourceId(102)).isEqualTo(2);
    assertThat(TestDataQuery.getDividendAnnouncementDataSourceCurrencyMappingsByDataSourceId(103)).isEqualTo(3);
  }

  @Test
  void deletePreConfiguredDataSource_badRequest() throws Exception {
    long id = 1;
    long dataSourceCount = dataSourceRepository.count();
    MvcResult mvcResult = deleteDataSource(id).andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
    assertThat(dataSourceRepository.count()).isEqualTo(dataSourceCount);
    assertThat(dataSourceRepository.existsById(id)).isTrue();
  }

  private ResultActions deleteDataSource(long id) throws Exception {
    return mockMvc.perform(delete(String.format(ENDPOINT, id)));
  }
}
