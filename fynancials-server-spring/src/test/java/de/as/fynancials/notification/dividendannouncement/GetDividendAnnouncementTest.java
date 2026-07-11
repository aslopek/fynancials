package de.as.fynancials.notification.dividendannouncement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.SqlMergeMode.MergeMode.MERGE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.as.fynancials.notification.dividendannouncement.api.model.DividendAnnouncementReadDto;
import integration.IntegrationTest;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@IntegrationTest
public class GetDividendAnnouncementTest {

  private static final String ENDPOINT = "/notifications/dividend-announcements";
  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Autowired
  private MockMvc mockMvc;

  @Test
  void getDividendAnnouncements_someContent_noQueryParam_ok() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get(ENDPOINT)).andExpect(status().isOk()).andReturn();

    List<DividendAnnouncementReadDto> responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});
    assertThat(responseBody).hasSize(9);

    DividendAnnouncementReadDto dividendAnnouncement = responseBody.getFirst();
    assertThat(dividendAnnouncement.getId()).isEqualTo(1);
    assertThat(dividendAnnouncement.getDataSourceId()).isEqualTo(101);
    assertThat(dividendAnnouncement.getSecurityId()).isEqualTo(1);
    assertThat(dividendAnnouncement.getIsNew()).isEqualTo(false);
    assertThat(dividendAnnouncement.getPayDate()).isEqualTo(LocalDate.of(2023, Month.SEPTEMBER, 14));
    assertThat(dividendAnnouncement.getAmountPerShare()).isEqualByComparingTo("0.68");
    assertThat(dividendAnnouncement.getCurrency()).isEqualTo("USD");

    dividendAnnouncement = responseBody.get(1);
    assertThat(dividendAnnouncement.getId()).isEqualTo(2);
    assertThat(dividendAnnouncement.getDataSourceId()).isEqualTo(101);
    assertThat(dividendAnnouncement.getSecurityId()).isEqualTo(1);
    assertThat(dividendAnnouncement.getIsNew()).isEqualTo(false);
    assertThat(dividendAnnouncement.getPayDate()).isEqualTo(LocalDate.of(2023, Month.DECEMBER, 14));
    assertThat(dividendAnnouncement.getAmountPerShare()).isEqualByComparingTo("0.75");
    assertThat(dividendAnnouncement.getCurrency()).isEqualTo("USD");

    dividendAnnouncement = responseBody.get(2);
    assertThat(dividendAnnouncement.getId()).isEqualTo(3);
    assertThat(dividendAnnouncement.getDataSourceId()).isEqualTo(101);
    assertThat(dividendAnnouncement.getSecurityId()).isEqualTo(1);
    assertThat(dividendAnnouncement.getIsNew()).isEqualTo(false);
    assertThat(dividendAnnouncement.getPayDate()).isEqualTo(LocalDate.of(2024, Month.MARCH, 14));
    assertThat(dividendAnnouncement.getAmountPerShare()).isEqualByComparingTo("0.75");
    assertThat(dividendAnnouncement.getCurrency()).isEqualTo("USD");

    dividendAnnouncement = responseBody.get(3);
    assertThat(dividendAnnouncement.getId()).isEqualTo(9);
    assertThat(dividendAnnouncement.getDataSourceId()).isEqualTo(103);
    assertThat(dividendAnnouncement.getSecurityId()).isEqualTo(47);
    assertThat(dividendAnnouncement.getIsNew()).isEqualTo(true);
    assertThat(dividendAnnouncement.getPayDate()).isEqualTo(LocalDate.of(2024, Month.APRIL, 21));
    assertThat(dividendAnnouncement.getAmountPerShare()).isEqualByComparingTo("13.4671");
    assertThat(dividendAnnouncement.getCurrency()).isEqualTo("ILS");

    dividendAnnouncement = responseBody.get(4);
    assertThat(dividendAnnouncement.getId()).isEqualTo(6);
    assertThat(dividendAnnouncement.getDataSourceId()).isEqualTo(102);
    assertThat(dividendAnnouncement.getSecurityId()).isEqualTo(3);
    assertThat(dividendAnnouncement.getIsNew()).isEqualTo(false);
    assertThat(dividendAnnouncement.getPayDate()).isEqualTo(LocalDate.of(2024, Month.MAY, 22));
    assertThat(dividendAnnouncement.getAmountPerShare()).isEqualByComparingTo("0.4");
    assertThat(dividendAnnouncement.getCurrency()).isEqualTo("EUR");

    dividendAnnouncement = responseBody.get(5);
    assertThat(dividendAnnouncement.getId()).isEqualTo(7);
    assertThat(dividendAnnouncement.getDataSourceId()).isEqualTo(102);
    assertThat(dividendAnnouncement.getSecurityId()).isEqualTo(37);
    assertThat(dividendAnnouncement.getIsNew()).isEqualTo(true);
    assertThat(dividendAnnouncement.getPayDate()).isEqualTo(LocalDate.of(2024, Month.JUNE, 4));
    assertThat(dividendAnnouncement.getAmountPerShare()).isEqualByComparingTo("9.06");
    assertThat(dividendAnnouncement.getCurrency()).isEqualTo("EUR");

    dividendAnnouncement = responseBody.get(6);
    assertThat(dividendAnnouncement.getId()).isEqualTo(8);
    assertThat(dividendAnnouncement.getDataSourceId()).isEqualTo(102);
    assertThat(dividendAnnouncement.getSecurityId()).isEqualTo(38);
    assertThat(dividendAnnouncement.getIsNew()).isEqualTo(true);
    assertThat(dividendAnnouncement.getPayDate()).isEqualTo(LocalDate.of(2024, Month.JUNE, 4));
    assertThat(dividendAnnouncement.getAmountPerShare()).isEqualByComparingTo("9");
    assertThat(dividendAnnouncement.getCurrency()).isEqualTo("EUR");

    dividendAnnouncement = responseBody.get(7);
    assertThat(dividendAnnouncement.getId()).isEqualTo(4);
    assertThat(dividendAnnouncement.getDataSourceId()).isEqualTo(101);
    assertThat(dividendAnnouncement.getSecurityId()).isEqualTo(1);
    assertThat(dividendAnnouncement.getIsNew()).isEqualTo(true);
    assertThat(dividendAnnouncement.getPayDate()).isEqualTo(LocalDate.of(2024, Month.JUNE, 13));
    assertThat(dividendAnnouncement.getAmountPerShare()).isEqualByComparingTo("0.75");
    assertThat(dividendAnnouncement.getCurrency()).isEqualTo("USD");

    dividendAnnouncement = responseBody.get(8);
    assertThat(dividendAnnouncement.getId()).isEqualTo(5);
    assertThat(dividendAnnouncement.getDataSourceId()).isEqualTo(101);
    assertThat(dividendAnnouncement.getSecurityId()).isEqualTo(1);
    assertThat(dividendAnnouncement.getIsNew()).isEqualTo(true);
    assertThat(dividendAnnouncement.getPayDate()).isEqualTo(LocalDate.of(2024, Month.SEPTEMBER, 12));
    assertThat(dividendAnnouncement.getAmountPerShare()).isEqualByComparingTo("0.75");
    assertThat(dividendAnnouncement.getCurrency()).isEqualTo("USD");
  }

  @Test
  void getDividendAnnouncements_someContent_isNew_ok() throws Exception {
    String url = String.format("%s?%s", ENDPOINT, "isNew=true");
    MvcResult mvcResult = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();

    List<DividendAnnouncementReadDto> responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});
    assertThat(responseBody).hasSize(5);

    DividendAnnouncementReadDto dividendAnnouncement = responseBody.getFirst();
    assertThat(dividendAnnouncement.getId()).isEqualTo(9);
    assertThat(dividendAnnouncement.getDataSourceId()).isEqualTo(103);
    assertThat(dividendAnnouncement.getSecurityId()).isEqualTo(47);
    assertThat(dividendAnnouncement.getIsNew()).isEqualTo(true);
    assertThat(dividendAnnouncement.getPayDate()).isEqualTo(LocalDate.of(2024, Month.APRIL, 21));
    assertThat(dividendAnnouncement.getAmountPerShare()).isEqualByComparingTo("13.4671");
    assertThat(dividendAnnouncement.getCurrency()).isEqualTo("ILS");

    dividendAnnouncement = responseBody.get(1);
    assertThat(dividendAnnouncement.getId()).isEqualTo(7);
    assertThat(dividendAnnouncement.getDataSourceId()).isEqualTo(102);
    assertThat(dividendAnnouncement.getSecurityId()).isEqualTo(37);
    assertThat(dividendAnnouncement.getIsNew()).isEqualTo(true);
    assertThat(dividendAnnouncement.getPayDate()).isEqualTo(LocalDate.of(2024, Month.JUNE, 4));
    assertThat(dividendAnnouncement.getAmountPerShare()).isEqualByComparingTo("9.06");
    assertThat(dividendAnnouncement.getCurrency()).isEqualTo("EUR");

    dividendAnnouncement = responseBody.get(2);
    assertThat(dividendAnnouncement.getId()).isEqualTo(8);
    assertThat(dividendAnnouncement.getDataSourceId()).isEqualTo(102);
    assertThat(dividendAnnouncement.getSecurityId()).isEqualTo(38);
    assertThat(dividendAnnouncement.getIsNew()).isEqualTo(true);
    assertThat(dividendAnnouncement.getPayDate()).isEqualTo(LocalDate.of(2024, Month.JUNE, 4));
    assertThat(dividendAnnouncement.getAmountPerShare()).isEqualByComparingTo("9");
    assertThat(dividendAnnouncement.getCurrency()).isEqualTo("EUR");

    dividendAnnouncement = responseBody.get(3);
    assertThat(dividendAnnouncement.getId()).isEqualTo(4);
    assertThat(dividendAnnouncement.getDataSourceId()).isEqualTo(101);
    assertThat(dividendAnnouncement.getSecurityId()).isEqualTo(1);
    assertThat(dividendAnnouncement.getIsNew()).isEqualTo(true);
    assertThat(dividendAnnouncement.getPayDate()).isEqualTo(LocalDate.of(2024, Month.JUNE, 13));
    assertThat(dividendAnnouncement.getAmountPerShare()).isEqualByComparingTo("0.75");
    assertThat(dividendAnnouncement.getCurrency()).isEqualTo("USD");

    dividendAnnouncement = responseBody.get(4);
    assertThat(dividendAnnouncement.getId()).isEqualTo(5);
    assertThat(dividendAnnouncement.getDataSourceId()).isEqualTo(101);
    assertThat(dividendAnnouncement.getSecurityId()).isEqualTo(1);
    assertThat(dividendAnnouncement.getIsNew()).isEqualTo(true);
    assertThat(dividendAnnouncement.getPayDate()).isEqualTo(LocalDate.of(2024, Month.SEPTEMBER, 12));
    assertThat(dividendAnnouncement.getAmountPerShare()).isEqualByComparingTo("0.75");
    assertThat(dividendAnnouncement.getCurrency()).isEqualTo("USD");
  }

  @Test
  void getDividendAnnouncements_someContent_isNotNew_ok() throws Exception {
    String url = String.format("%s?%s", ENDPOINT, "isNew=false");
    MvcResult mvcResult = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();

    List<DividendAnnouncementReadDto> responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});
    assertThat(responseBody).hasSize(4);

    DividendAnnouncementReadDto dividendAnnouncement = responseBody.getFirst();
    assertThat(dividendAnnouncement.getId()).isEqualTo(1);
    assertThat(dividendAnnouncement.getDataSourceId()).isEqualTo(101);
    assertThat(dividendAnnouncement.getSecurityId()).isEqualTo(1);
    assertThat(dividendAnnouncement.getIsNew()).isEqualTo(false);
    assertThat(dividendAnnouncement.getPayDate()).isEqualTo(LocalDate.of(2023, Month.SEPTEMBER, 14));
    assertThat(dividendAnnouncement.getAmountPerShare()).isEqualByComparingTo("0.68");
    assertThat(dividendAnnouncement.getCurrency()).isEqualTo("USD");

    dividendAnnouncement = responseBody.get(1);
    assertThat(dividendAnnouncement.getId()).isEqualTo(2);
    assertThat(dividendAnnouncement.getDataSourceId()).isEqualTo(101);
    assertThat(dividendAnnouncement.getSecurityId()).isEqualTo(1);
    assertThat(dividendAnnouncement.getIsNew()).isEqualTo(false);
    assertThat(dividendAnnouncement.getPayDate()).isEqualTo(LocalDate.of(2023, Month.DECEMBER, 14));
    assertThat(dividendAnnouncement.getAmountPerShare()).isEqualByComparingTo("0.75");
    assertThat(dividendAnnouncement.getCurrency()).isEqualTo("USD");

    dividendAnnouncement = responseBody.get(2);
    assertThat(dividendAnnouncement.getId()).isEqualTo(3);
    assertThat(dividendAnnouncement.getDataSourceId()).isEqualTo(101);
    assertThat(dividendAnnouncement.getSecurityId()).isEqualTo(1);
    assertThat(dividendAnnouncement.getIsNew()).isEqualTo(false);
    assertThat(dividendAnnouncement.getPayDate()).isEqualTo(LocalDate.of(2024, Month.MARCH, 14));
    assertThat(dividendAnnouncement.getAmountPerShare()).isEqualByComparingTo("0.75");
    assertThat(dividendAnnouncement.getCurrency()).isEqualTo("USD");

    dividendAnnouncement = responseBody.get(3);
    assertThat(dividendAnnouncement.getId()).isEqualTo(6);
    assertThat(dividendAnnouncement.getDataSourceId()).isEqualTo(102);
    assertThat(dividendAnnouncement.getSecurityId()).isEqualTo(3);
    assertThat(dividendAnnouncement.getIsNew()).isEqualTo(false);
    assertThat(dividendAnnouncement.getPayDate()).isEqualTo(LocalDate.of(2024, Month.MAY, 22));
    assertThat(dividendAnnouncement.getAmountPerShare()).isEqualByComparingTo("0.4");
    assertThat(dividendAnnouncement.getCurrency()).isEqualTo("EUR");
  }

  @Test
  @SqlMergeMode(MERGE)
  @Sql(statements = "DELETE FROM DIVIDEND_ANNOUNCEMENT")
  void getDividendAnnouncements_noContent_ok() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get(ENDPOINT)).andExpect(status().isOk()).andReturn();
    assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("[]");
  }
}
