package de.as.fynancials.depot.performance.income;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.as.fynancials.depot.performance.api.model.IncomeTypeDto;
import de.as.fynancials.depot.performance.api.model.PerformanceDto;
import de.as.fynancials.depot.performance.api.model.TransactionReferenceDto;
import integration.IntegrationTest;
import integration.SecurityIds;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@IntegrationTest
class GetIncomeTest {

  private static final String ENDPOINT = "/depot-performance/income?depots=%s&securities=%s&incomeTypes=%s";

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Autowired
  private MockMvc mockMvc;

  @Test
  void getDividends_firstDepot_ok() throws Exception {
    String url =
        String.format(ENDPOINT, "1", String.format("%d,%d,%d", SecurityIds.NVDA, SecurityIds.LVMH, SecurityIds.AMZN),
            IncomeTypeDto.DIVIDEND);
    MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(url)).andExpect(status().isOk()).andReturn();
    List<PerformanceDto> responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

    assertThat(responseBody).hasSize(2);
    verifyDepot1_dividend_nvda(responseBody);
    verifyDepot1_dividend_lvmh(responseBody);
  }

  @Test
  void getDividends_firstAndSecondDepot_ok() throws Exception {
    String url = String.format(ENDPOINT, "1,2",
        String.format("%d,%d,%d,%d", SecurityIds.NVDA, SecurityIds.LVMH, SecurityIds.AMZN, SecurityIds.HAG),
        IncomeTypeDto.DIVIDEND);
    MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(url)).andExpect(status().isOk()).andReturn();
    List<PerformanceDto> responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

    assertThat(responseBody).hasSize(3);
    verifyDepot1_dividend_lvmh(responseBody);

    PerformanceDto hag = getPerformance(responseBody, SecurityIds.HAG);
    assertThat(hag.getSecurityIds()).containsExactlyInAnyOrder(SecurityIds.HAG);
    assertThat(hag.getAbsoluteValueGross()).isEqualTo(150.5);
    assertThat(hag.getAbsoluteValueNet()).isEqualTo(150.5);
    List<TransactionReferenceDto> transactions = hag.getTransactions();
    assertThat(transactions).hasSize(3);
    assertThat(transactions.get(0).getDepotId()).isEqualTo(2);
    assertThat(transactions.get(0).getTransactionId()).isEqualTo(13);
    assertThat(transactions.get(1).getDepotId()).isEqualTo(2);
    assertThat(transactions.get(1).getTransactionId()).isEqualTo(22);
    assertThat(transactions.get(2).getDepotId()).isEqualTo(2);
    assertThat(transactions.get(2).getTransactionId()).isEqualTo(23);

    PerformanceDto nvda = getPerformance(responseBody, SecurityIds.NVDA);
    assertThat(nvda.getSecurityIds()).containsExactlyInAnyOrder(SecurityIds.NVDA);
    assertThat(nvda.getAbsoluteValueGross()).isEqualTo(12.18);
    assertThat(nvda.getAbsoluteValueNet()).isEqualTo(9.06);
    transactions = nvda.getTransactions();
    assertThat(transactions).hasSize(9);
    assertThat(transactions.get(0).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(0).getTransactionId()).isEqualTo(8);
    assertThat(transactions.get(1).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(1).getTransactionId()).isEqualTo(10);
    assertThat(transactions.get(2).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(2).getTransactionId()).isEqualTo(14);
    assertThat(transactions.get(3).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(3).getTransactionId()).isEqualTo(16);
    assertThat(transactions.get(4).getDepotId()).isEqualTo(2);
    assertThat(transactions.get(4).getTransactionId()).isEqualTo(18);
    assertThat(transactions.get(5).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(5).getTransactionId()).isEqualTo(19);
    assertThat(transactions.get(6).getDepotId()).isEqualTo(2);
    assertThat(transactions.get(6).getTransactionId()).isEqualTo(21);
    assertThat(transactions.get(7).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(7).getTransactionId()).isEqualTo(39);
    assertThat(transactions.get(8).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(8).getTransactionId()).isEqualTo(32);
  }

  @Test
  void getDividendsAndSells_firstDepot_ok() throws Exception {
    String url = String.format(ENDPOINT, "1",
        String.format("%d,%d,%d,%d", SecurityIds.NVDA, SecurityIds.LVMH, SecurityIds.AMZN, SecurityIds.HAG),
        String.format("%s,%s", IncomeTypeDto.DIVIDEND, IncomeTypeDto.SELL));
    MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(url)).andExpect(status().isOk()).andReturn();
    List<PerformanceDto> responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

    assertThat(responseBody).hasSize(4);
    verifyDepot1_dividend_lvmh(responseBody);

    PerformanceDto hag = getPerformance(responseBody, SecurityIds.HAG);
    assertThat(hag.getSecurityIds()).containsExactlyInAnyOrder(SecurityIds.HAG);
    assertThat(hag.getAbsoluteValueGross()).isEqualTo(1764.0);
    assertThat(hag.getAbsoluteValueNet()).isEqualTo(1747.58);
    List<TransactionReferenceDto> transactions = hag.getTransactions();
    assertThat(transactions).hasSize(1);
    assertThat(transactions.get(0).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(0).getTransactionId()).isEqualTo(11);

    PerformanceDto nvda = getPerformance(responseBody, SecurityIds.NVDA);
    assertThat(nvda.getSecurityIds()).containsExactlyInAnyOrder(SecurityIds.NVDA);
    assertThat(nvda.getAbsoluteValueGross()).isEqualTo(1051.83);
    assertThat(nvda.getAbsoluteValueNet()).isEqualTo(1015.42);
    transactions = nvda.getTransactions();
    assertThat(transactions).hasSize(8);
    assertThat(transactions.get(0).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(0).getTransactionId()).isEqualTo(3);
    assertThat(transactions.get(1).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(1).getTransactionId()).isEqualTo(8);
    assertThat(transactions.get(2).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(2).getTransactionId()).isEqualTo(10);
    assertThat(transactions.get(3).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(3).getTransactionId()).isEqualTo(14);
    assertThat(transactions.get(4).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(4).getTransactionId()).isEqualTo(16);
    assertThat(transactions.get(5).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(5).getTransactionId()).isEqualTo(19);
    assertThat(transactions.get(6).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(6).getTransactionId()).isEqualTo(39);
    assertThat(transactions.get(7).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(7).getTransactionId()).isEqualTo(32);

    PerformanceDto amzn = getPerformance(responseBody, SecurityIds.AMZN);
    assertThat(amzn.getSecurityIds()).containsExactlyInAnyOrder(SecurityIds.AMZN);
    assertThat(amzn.getAbsoluteValueGross()).isEqualTo(3842.78);
    assertThat(amzn.getAbsoluteValueNet()).isEqualTo(3832.78);
    transactions = amzn.getTransactions();
    assertThat(transactions).hasSize(1);
    assertThat(transactions.get(0).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(0).getTransactionId()).isEqualTo(4);
  }

  @Test
  void getDividends_etf_noContent() throws Exception {
    String url = String.format(ENDPOINT, "4", SecurityIds.VNGGF, IncomeTypeDto.DIVIDEND);
    MvcResult mvcResult =
        mockMvc.perform(MockMvcRequestBuilders.get(url)).andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void getOther_etf_ok() throws Exception {
    String url = String.format(ENDPOINT, "4", SecurityIds.VNGGF, IncomeTypeDto.OTHER);
    MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(url)).andExpect(status().isOk()).andReturn();
    List<PerformanceDto> responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

    assertThat(responseBody).hasSize(1);
    PerformanceDto vnggf = getPerformance(responseBody, SecurityIds.VNGGF);
    assertThat(vnggf.getAbsoluteValueGross()).isEqualTo(-7.89);
    assertThat(vnggf.getAbsoluteValueNet()).isEqualTo(-7.89);
    List<TransactionReferenceDto> transactions = vnggf.getTransactions();
    assertThat(transactions).hasSize(1);
    assertThat(transactions.get(0).getDepotId()).isEqualTo(4);
    assertThat(transactions.get(0).getTransactionId()).isEqualTo(30);
  }

  @Test
  void getDividendsAndSells_securityGroupsAreConsolidated_ok() throws Exception {
    String url = String.format(ENDPOINT, "7,8",
        String.format("%d,%d,%d", SecurityIds.VW_VZ, SecurityIds.VW_STAMM, SecurityIds.GOOGL),
        String.format("%s,%s", IncomeTypeDto.DIVIDEND, IncomeTypeDto.SELL));
    MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(url)).andExpect(status().isOk()).andReturn();
    List<PerformanceDto> responseBody =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

    assertThat(responseBody).hasSize(2);

    PerformanceDto googl = getPerformance(responseBody, SecurityIds.GOOGL);
    assertThat(googl.getSecurityIds()).containsExactlyInAnyOrder(SecurityIds.GOOGL);
    assertThat(googl.getAbsoluteValueGross()).isEqualTo(2105.64);
    assertThat(googl.getAbsoluteValueNet()).isEqualTo(1809.85);
    List<TransactionReferenceDto> transactions = googl.getTransactions();
    assertThat(transactions).hasSize(1);
    assertThat(transactions.get(0).getDepotId()).isEqualTo(7);
    assertThat(transactions.get(0).getTransactionId()).isEqualTo(104);

    PerformanceDto volkswagen = getPerformance(responseBody, SecurityIds.VW_VZ);
    assertThat(volkswagen.getSecurityIds()).containsExactlyInAnyOrder(SecurityIds.VW_VZ, SecurityIds.VW_STAMM);
    assertThat(volkswagen.getAbsoluteValueGross()).isEqualTo(935.64);
    assertThat(volkswagen.getAbsoluteValueNet()).isEqualTo(701.7);
    transactions = volkswagen.getTransactions();
    assertThat(transactions).hasSize(12);
    assertThat(transactions.get(0).getDepotId()).isEqualTo(7);
    assertThat(transactions.get(0).getTransactionId()).isEqualTo(110);
    assertThat(transactions.get(1).getDepotId()).isEqualTo(7);
    assertThat(transactions.get(1).getTransactionId()).isEqualTo(111);
    assertThat(transactions.get(2).getDepotId()).isEqualTo(8);
    assertThat(transactions.get(2).getTransactionId()).isEqualTo(210);
    assertThat(transactions.get(3).getDepotId()).isEqualTo(7);
    assertThat(transactions.get(3).getTransactionId()).isEqualTo(112);
    assertThat(transactions.get(4).getDepotId()).isEqualTo(7);
    assertThat(transactions.get(4).getTransactionId()).isEqualTo(113);
    assertThat(transactions.get(5).getDepotId()).isEqualTo(8);
    assertThat(transactions.get(5).getTransactionId()).isEqualTo(212);
    assertThat(transactions.get(6).getDepotId()).isEqualTo(7);
    assertThat(transactions.get(6).getTransactionId()).isEqualTo(114);
    assertThat(transactions.get(7).getDepotId()).isEqualTo(7);
    assertThat(transactions.get(7).getTransactionId()).isEqualTo(115);
    assertThat(transactions.get(8).getDepotId()).isEqualTo(8);
    assertThat(transactions.get(8).getTransactionId()).isEqualTo(214);
    assertThat(transactions.get(9).getDepotId()).isEqualTo(7);
    assertThat(transactions.get(9).getTransactionId()).isEqualTo(116);
    assertThat(transactions.get(10).getDepotId()).isEqualTo(7);
    assertThat(transactions.get(10).getTransactionId()).isEqualTo(117);
    assertThat(transactions.get(11).getDepotId()).isEqualTo(8);
    assertThat(transactions.get(11).getTransactionId()).isEqualTo(216);
  }

  @Test
  void getDividends_depotIdsEmpty_badRequest() throws Exception {
    MvcResult mvcResult =
        mockMvc.perform(MockMvcRequestBuilders.get(String.format(ENDPOINT, "", "1", IncomeTypeDto.DIVIDEND)))
            .andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void getDividends_depotIdsNotSet_badRequest() throws Exception {
    MvcResult mvcResult =
        mockMvc.perform(MockMvcRequestBuilders.get("/depot-performance/income?securities=1&incomeTypes=DIVIDEND"))
            .andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void getDividends_securityIdsEmpty_badRequest() throws Exception {
    MvcResult mvcResult =
        mockMvc.perform(MockMvcRequestBuilders.get(String.format(ENDPOINT, "1", "", IncomeTypeDto.DIVIDEND)))
            .andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void getDividends_securityIdsNotSet_badRequest() throws Exception {
    MvcResult mvcResult =
        mockMvc.perform(MockMvcRequestBuilders.get("/depot-performance/income?depots=1&incomeTypes=DIVIDEND"))
            .andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void incomeTypeNotSet() throws Exception {
    MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/depot-performance/income?depots=1&securities=1"))
        .andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void getDividends_depotsHaveDifferentCurrencies_badRequest() throws Exception {
    MvcResult mvcResult =
        mockMvc.perform(MockMvcRequestBuilders.get(String.format(ENDPOINT, "4,5", "1", IncomeTypeDto.DIVIDEND)))
            .andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  private PerformanceDto getPerformance(List<PerformanceDto> performances, Long securityId) {
    for (PerformanceDto performance : performances) {
      if (performance.getSecurityIds().contains(securityId)) {
        return performance;
      }
    }
    fail(String.format("Did not find performance for securityId %d", securityId));
    throw new RuntimeException();
  }

  private void verifyDepot1_dividend_nvda(List<PerformanceDto> responseBody) {
    PerformanceDto nvda = getPerformance(responseBody, SecurityIds.NVDA);
    assertThat(nvda.getSecurityIds()).containsExactlyInAnyOrder(SecurityIds.NVDA);
    assertThat(nvda.getAbsoluteValueGross()).isEqualTo(11.79);
    assertThat(nvda.getAbsoluteValueNet()).isEqualTo(8.73);
    List<TransactionReferenceDto> transactions = nvda.getTransactions();
    assertThat(transactions).hasSize(7);
    assertThat(transactions.get(0).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(0).getTransactionId()).isEqualTo(8);
    assertThat(transactions.get(1).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(1).getTransactionId()).isEqualTo(10);
    assertThat(transactions.get(2).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(2).getTransactionId()).isEqualTo(14);
    assertThat(transactions.get(3).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(3).getTransactionId()).isEqualTo(16);
    assertThat(transactions.get(4).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(4).getTransactionId()).isEqualTo(19);
    assertThat(transactions.get(5).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(5).getTransactionId()).isEqualTo(39);
    assertThat(transactions.get(6).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(6).getTransactionId()).isEqualTo(32);
  }

  private void verifyDepot1_dividend_lvmh(List<PerformanceDto> responseBody) {
    PerformanceDto lvmh = getPerformance(responseBody, SecurityIds.LVMH);
    assertThat(lvmh.getSecurityIds()).containsExactlyInAnyOrder(SecurityIds.LVMH);
    assertThat(lvmh.getAbsoluteValueGross()).isEqualTo(45.06);
    assertThat(lvmh.getAbsoluteValueNet()).isEqualTo(33.49);
    List<TransactionReferenceDto> transactions = lvmh.getTransactions();
    assertThat(transactions).hasSize(2);
    assertThat(transactions.get(0).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(0).getTransactionId()).isEqualTo(26);
    assertThat(transactions.get(1).getDepotId()).isEqualTo(1);
    assertThat(transactions.get(1).getTransactionId()).isEqualTo(31);
  }
}
