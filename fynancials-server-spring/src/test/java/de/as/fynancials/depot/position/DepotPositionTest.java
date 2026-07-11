package de.as.fynancials.depot.position;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.as.fynancials.depot.position.api.model.DepotCompositionDto;
import de.as.fynancials.depot.position.api.model.DepotPositionDto;
import integration.IntegrationTest;
import integration.SecurityIds;
import java.util.List;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class DepotPositionTest {

  private static final String ENDPOINT = "/depot-positions?depots=%s";
  private static final Offset<Double> ACCURACY_ONE_THOUSANDTH = Offset.strictOffset(0.001);
  private static final Offset<Double> ACCURACY_ONE_HUNDREDTH = Offset.offset(0.01);

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Autowired
  private MockMvc mockMvc;

  @Test
  void getDepotPositions_empty_ok() throws Exception {
    MvcResult result = getDepotPositions("3").andExpect(status().isOk()).andReturn();
    DepotCompositionDto responseBody = objectMapper.readValue(result.getResponse().getContentAsString(),
        DepotCompositionDto.class);
    assertThat(responseBody.getCurrency()).isEqualTo("EUR");
    assertThat(responseBody.getBuyInAbsolute()).isEqualTo(0.0);
    assertThat(responseBody.getCurrentSizeAbsolute()).isEqualTo(0.0);
    assertThat(responseBody.getAbsolutePerformance()).isEqualTo(0.0);
    assertThat(responseBody.getRelativePerformance()).isNull();
    List<DepotPositionDto> positions = responseBody.getPositions();
    assertThat(positions).isEmpty();
  }

  @Test
  void getDepotPositions_doesNotExist_badRequest() throws Exception {
    MvcResult result = getDepotPositions("999").andExpect(status().isBadRequest()).andReturn();
    assertThat(result.getResponse().getContentLength()).isZero();
  }

  @Test
  void getDepotPositions_noDepotsSupplied_badRequest() throws Exception {
    MvcResult result = getDepotPositions("").andExpect(status().isBadRequest()).andReturn();
    assertThat(result.getResponse().getContentLength()).isZero();
  }

  @Test
  void getDepotPositions_firstDepot_ok() throws Exception {
    MvcResult result = getDepotPositions("1").andExpect(status().isOk()).andReturn();
    DepotCompositionDto responseBody = objectMapper.readValue(result.getResponse().getContentAsString(),
        DepotCompositionDto.class);
    assertThat(responseBody.getCurrency()).isEqualTo("EUR");
    assertThat(responseBody.getBuyInAbsolute()).isEqualTo(13633.71);
    assertThat(responseBody.getCurrentSizeAbsolute()).isEqualTo(13895.87);
    assertThat(responseBody.getAbsolutePerformance()).isEqualTo(262.16);
    assertThat(responseBody.getRelativePerformance()).isCloseTo(1.92, ACCURACY_ONE_HUNDREDTH);

    List<DepotPositionDto> positions = responseBody.getPositions();
    assertThat(positions).isNotNull();
    assertThat(positions).hasSize(3);

    DepotPositionDto position = positions.getFirst();
    assertThat(position.getSecurityIds()).containsExactlyInAnyOrder(SecurityIds.AMZN);
    assertThat(position.getDisplayName()).isEqualTo("Amazon");
    assertThat(position.getSecurityGroupId()).isNull();
    assertThat(position.getCount()).isEqualTo(54.0);
    assertThat(position.getBuyInAbsolute()).isEqualTo(7221.16);
    assertThat(position.getBuyInRelative()).isCloseTo(52.96548, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getCurrentSizeAbsolute()).isEqualTo(7483.32);
    assertThat(position.getCurrentSizeRelative()).isCloseTo(53.85284, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getAbsolutePerformance()).isEqualTo(262.16);
    assertThat(position.getRelativePerformance()).isCloseTo(3.63044, ACCURACY_ONE_THOUSANDTH);

    position = positions.get(1);
    assertThat(position.getSecurityIds()).containsExactlyInAnyOrder(SecurityIds.NVDA);
    assertThat(position.getDisplayName()).isEqualTo("Nvidia");
    assertThat(position.getSecurityGroupId()).isNull();
    assertThat(position.getCount()).isEqualTo(21.6);
    assertThat(position.getBuyInAbsolute()).isEqualTo(3317.44);
    assertThat(position.getBuyInRelative()).isCloseTo(24.33263, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getCurrentSizeAbsolute()).isEqualTo(3317.44);
    assertThat(position.getCurrentSizeRelative()).isCloseTo(23.87357, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getAbsolutePerformance()).isEqualTo(0);
    assertThat(position.getRelativePerformance()).isEqualTo(0);

    position = positions.get(2);
    assertThat(position.getSecurityIds()).containsExactlyInAnyOrder(SecurityIds.LVMH);
    assertThat(position.getDisplayName()).isEqualTo("LVMH");
    assertThat(position.getSecurityGroupId()).isNull();
    assertThat(position.getCount()).isEqualTo(4.25);
    assertThat(position.getBuyInAbsolute()).isEqualTo(3095.11);
    assertThat(position.getBuyInRelative()).isCloseTo(22.70189, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getCurrentSizeAbsolute()).isEqualTo(3095.11);
    assertThat(position.getCurrentSizeRelative()).isCloseTo(22.2736, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getAbsolutePerformance()).isEqualTo(0);
    assertThat(position.getRelativePerformance()).isEqualTo(0);
  }

  @Test
  void getDepotPositions_otherDepot_ok() throws Exception {
    MvcResult result = getDepotPositions("2").andExpect(status().isOk()).andReturn();
    DepotCompositionDto responseBody = objectMapper.readValue(result.getResponse().getContentAsString(),
        DepotCompositionDto.class);
    assertThat(responseBody.getCurrency()).isEqualTo("EUR");
    assertThat(responseBody.getBuyInAbsolute()).isEqualTo(4143.28);
    assertThat(responseBody.getCurrentSizeAbsolute()).isEqualTo(4143.28);
    assertThat(responseBody.getAbsolutePerformance()).isEqualTo(0.0);
    assertThat(responseBody.getRelativePerformance()).isEqualTo(0.0);

    List<DepotPositionDto> positions = responseBody.getPositions();
    assertThat(positions).isNotNull();
    assertThat(positions).hasSize(2);

    DepotPositionDto position = positions.getFirst();
    assertThat(position.getSecurityIds()).containsExactlyInAnyOrder(SecurityIds.HAG);
    assertThat(position.getDisplayName()).isEqualTo("Hensoldt");
    assertThat(position.getSecurityGroupId()).isNull();
    assertThat(position.getCount()).isEqualTo(250.0);
    assertThat(position.getBuyInAbsolute()).isEqualTo(3159.0);
    assertThat(position.getBuyInRelative()).isCloseTo(76.24394, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getCurrentSizeAbsolute()).isEqualTo(3159.0);
    assertThat(position.getCurrentSizeRelative()).isCloseTo(76.24394, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getAbsolutePerformance()).isEqualTo(0.0);
    assertThat(position.getRelativePerformance()).isEqualTo(0.0);

    position = positions.get(1);
    assertThat(position.getSecurityIds()).containsExactlyInAnyOrder(SecurityIds.NVDA);
    assertThat(position.getDisplayName()).isEqualTo("Nvidia");
    assertThat(position.getSecurityGroupId()).isNull();
    assertThat(position.getCount()).isEqualTo(5.5);
    assertThat(position.getBuyInAbsolute()).isEqualTo(984.28);
    assertThat(position.getBuyInRelative()).isCloseTo(23.75606, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getCurrentSizeAbsolute()).isEqualTo(984.28);
    assertThat(position.getCurrentSizeRelative()).isCloseTo(23.75606, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getAbsolutePerformance()).isEqualTo(0.0);
    assertThat(position.getRelativePerformance()).isEqualTo(0.0);
  }

  @Test
  void getDepotPositions_etf_ok() throws Exception {
    MvcResult result = getDepotPositions("4").andExpect(status().isOk()).andReturn();
    DepotCompositionDto responseBody = objectMapper.readValue(result.getResponse().getContentAsString(),
        DepotCompositionDto.class);
    assertThat(responseBody.getCurrency()).isEqualTo("EUR");
    assertThat(responseBody.getBuyInAbsolute()).isEqualTo(2849.82);
    assertThat(responseBody.getCurrentSizeAbsolute()).isEqualTo(2849.82);
    assertThat(responseBody.getAbsolutePerformance()).isEqualTo(0.0);
    assertThat(responseBody.getRelativePerformance()).isEqualTo(0.0);

    List<DepotPositionDto> positions = responseBody.getPositions();
    assertThat(positions).isNotNull();
    assertThat(positions).hasSize(1);

    DepotPositionDto position = positions.getFirst();
    assertThat(position.getSecurityIds()).containsExactlyInAnyOrder(SecurityIds.VNGGF);
    assertThat(position.getDisplayName()).isEqualTo("Vanguard FTSE All-World High Dividend Yield UCITS DIST");
    assertThat(position.getSecurityGroupId()).isNull();
    assertThat(position.getCount()).isEqualTo(51.063);
    assertThat(position.getBuyInAbsolute()).isEqualTo(2849.82);
    assertThat(position.getBuyInRelative()).isEqualTo(100.0);
    assertThat(position.getCurrentSizeAbsolute()).isEqualTo(2849.82);
    assertThat(position.getCurrentSizeRelative()).isEqualTo(100.0);
    assertThat(position.getAbsolutePerformance()).isEqualTo(0.0);
    assertThat(position.getRelativePerformance()).isEqualTo(0.0);
  }

  @Test
  void getDepotPositions_usdDepot_ok() throws Exception {
    MvcResult result = getDepotPositions("5").andExpect(status().isOk()).andReturn();
    DepotCompositionDto responseBody = objectMapper.readValue(result.getResponse().getContentAsString(),
        DepotCompositionDto.class);
    assertThat(responseBody.getCurrency()).isEqualTo("USD");
    assertThat(responseBody.getBuyInAbsolute()).isEqualTo(4653.72);
    assertThat(responseBody.getCurrentSizeAbsolute()).isCloseTo(6165.66, ACCURACY_ONE_HUNDREDTH);
    assertThat(responseBody.getAbsolutePerformance()).isCloseTo(1511.94, ACCURACY_ONE_HUNDREDTH);
    assertThat(responseBody.getRelativePerformance()).isCloseTo(32.49, ACCURACY_ONE_HUNDREDTH);

    List<DepotPositionDto> positions = responseBody.getPositions();
    assertThat(positions).isNotNull();
    assertThat(positions).hasSize(2);

    DepotPositionDto position = positions.getFirst();
    assertThat(position.getSecurityIds()).containsExactlyInAnyOrder(SecurityIds.PINS);
    assertThat(position.getDisplayName()).isEqualTo("Pinterest");
    assertThat(position.getSecurityGroupId()).isNull();
    assertThat(position.getCount()).isEqualTo(130.5);
    assertThat(position.getBuyInAbsolute()).isEqualTo(3326.45);
    assertThat(position.getBuyInRelative()).isCloseTo(71.47938, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getCurrentSizeAbsolute()).isEqualTo(4833.72);
    assertThat(position.getCurrentSizeRelative()).isCloseTo(78.39744, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getAbsolutePerformance()).isEqualTo(1507.27);
    assertThat(position.getRelativePerformance()).isCloseTo(45.31167, ACCURACY_ONE_THOUSANDTH);

    position = positions.get(1);
    assertThat(position.getSecurityIds()).containsExactlyInAnyOrder(SecurityIds.GOOGL);
    assertThat(position.getDisplayName()).isEqualTo("Alphabet A");
    assertThat(position.getSecurityGroupId()).isEqualTo(2);
    assertThat(position.getCount()).isEqualTo(9.75);
    assertThat(position.getBuyInAbsolute()).isEqualTo(1327.27);
    assertThat(position.getBuyInRelative()).isCloseTo(28.52062, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getCurrentSizeAbsolute()).isCloseTo(1331.94019, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getCurrentSizeRelative()).isCloseTo(21.60256, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getAbsolutePerformance()).isCloseTo(4.67019, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getRelativePerformance()).isCloseTo(0.35186, ACCURACY_ONE_THOUSANDTH);
  }

  @Test
  void getDepotPositions_buyInZero_ok() throws Exception {
    MvcResult result = getDepotPositions("9").andExpect(status().isOk()).andReturn();
    DepotCompositionDto responseBody = objectMapper.readValue(result.getResponse().getContentAsString(),
        DepotCompositionDto.class);
    assertThat(responseBody.getCurrency()).isEqualTo("EUR");
    assertThat(responseBody.getBuyInAbsolute()).isEqualTo(0.0);
    assertThat(responseBody.getCurrentSizeAbsolute()).isEqualTo(5070.4);
    assertThat(responseBody.getAbsolutePerformance()).isEqualTo(5070.4);
    assertThat(responseBody.getRelativePerformance()).isEqualTo(null);

    List<DepotPositionDto> positions = responseBody.getPositions();
    assertThat(positions).isNotNull();
    assertThat(positions).hasSize(1);

    DepotPositionDto position = positions.getFirst();
    assertThat(position.getSecurityIds()).containsExactlyInAnyOrder(SecurityIds.GOOGL);
    assertThat(position.getDisplayName()).isEqualTo("Alphabet A");
    assertThat(position.getSecurityGroupId()).isEqualTo(2);
    assertThat(position.getCount()).isEqualTo(40.0);
    assertThat(position.getBuyInAbsolute()).isEqualTo(0.0);
    assertThat(position.getBuyInRelative()).isEqualTo(0.0);
    assertThat(position.getCurrentSizeAbsolute()).isEqualTo(5070.4);
    assertThat(position.getCurrentSizeRelative()).isEqualTo(100.0);
    assertThat(position.getAbsolutePerformance()).isEqualTo(5070.4);
    assertThat(position.getRelativePerformance()).isEqualTo(0.0);
  }

  @Test
  void getDepotPositions_groupedSecurities_ok() throws Exception {
    MvcResult result = getDepotPositions("7").andExpect(status().isOk()).andReturn();
    DepotCompositionDto responseBody = objectMapper.readValue(result.getResponse().getContentAsString(),
        DepotCompositionDto.class);
    assertThat(responseBody.getCurrency()).isEqualTo("EUR");
    assertThat(responseBody.getBuyInAbsolute()).isEqualTo(7248.74);
    assertThat(responseBody.getCurrentSizeAbsolute()).isCloseTo(7296.68, ACCURACY_ONE_HUNDREDTH);
    assertThat(responseBody.getAbsolutePerformance()).isCloseTo(47.94, ACCURACY_ONE_HUNDREDTH);
    assertThat(responseBody.getRelativePerformance()).isCloseTo(0.66, ACCURACY_ONE_HUNDREDTH);

    List<DepotPositionDto> positions = responseBody.getPositions();
    assertThat(positions).isNotNull();
    assertThat(positions).hasSize(2);

    DepotPositionDto position = positions.getFirst();
    assertThat(position.getSecurityIds()).containsExactlyInAnyOrder(SecurityIds.GOOGL, SecurityIds.GOOG);
    assertThat(position.getDisplayName()).isEqualTo("Alphabet");
    assertThat(position.getSecurityGroupId()).isEqualTo(2);
    assertThat(position.getCount()).isEqualTo(33.266);
    assertThat(position.getBuyInAbsolute()).isEqualTo(4297.74);
    assertThat(position.getBuyInRelative()).isCloseTo(59.28948, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getCurrentSizeAbsolute()).isCloseTo(4228.20616, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getCurrentSizeRelative()).isCloseTo(57.94698, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getAbsolutePerformance()).isCloseTo(-69.53384, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getRelativePerformance()).isCloseTo(-1.61792, ACCURACY_ONE_THOUSANDTH);

    position = positions.get(1);
    assertThat(position.getSecurityIds()).containsExactlyInAnyOrder(SecurityIds.VW_VZ, SecurityIds.VW_STAMM);
    assertThat(position.getDisplayName()).isEqualTo("Volkswagen");
    assertThat(position.getSecurityGroupId()).isEqualTo(1);
    assertThat(position.getCount()).isEqualTo(27.0);
    assertThat(position.getBuyInAbsolute()).isEqualTo(2951.0);
    assertThat(position.getBuyInRelative()).isCloseTo(40.71052, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getCurrentSizeAbsolute()).isEqualTo(3068.475);
    assertThat(position.getCurrentSizeRelative()).isCloseTo(42.05302, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getAbsolutePerformance()).isEqualTo(117.475);
    assertThat(position.getRelativePerformance()).isCloseTo(3.98085, ACCURACY_ONE_THOUSANDTH);
  }

  @Test
  void getDepotPositions_ungroupedSecurities_ok() throws Exception {
    MvcResult result = getDepotPositions("8").andExpect(status().isOk()).andReturn();
    DepotCompositionDto responseBody = objectMapper.readValue(result.getResponse().getContentAsString(),
        DepotCompositionDto.class);
    assertThat(responseBody.getCurrency()).isEqualTo("EUR");
    assertThat(responseBody.getBuyInAbsolute()).isEqualTo(3794.74);
    assertThat(responseBody.getCurrentSizeAbsolute()).isEqualTo(5379.42);
    assertThat(responseBody.getAbsolutePerformance()).isEqualTo(1584.68);
    assertThat(responseBody.getRelativePerformance()).isCloseTo(41.76, ACCURACY_ONE_HUNDREDTH);

    List<DepotPositionDto> positions = responseBody.getPositions();
    assertThat(positions).isNotNull();
    assertThat(positions).hasSize(2);

    DepotPositionDto position = positions.getFirst();
    assertThat(position.getSecurityIds()).containsExactlyInAnyOrder(SecurityIds.GOOGL);
    assertThat(position.getDisplayName()).isEqualTo("Alphabet A");
    assertThat(position.getSecurityGroupId()).isEqualTo(2);
    assertThat(position.getCount()).isEqualTo(34.5);
    assertThat(position.getBuyInAbsolute()).isEqualTo(2904.55);
    assertThat(position.getBuyInRelative()).isCloseTo(76.54148, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getCurrentSizeAbsolute()).isEqualTo(4373.22);
    assertThat(position.getCurrentSizeRelative()).isCloseTo(81.29538, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getAbsolutePerformance()).isEqualTo(1468.67);
    assertThat(position.getRelativePerformance()).isCloseTo(50.56446, ACCURACY_ONE_THOUSANDTH);

    position = positions.get(1);
    assertThat(position.getSecurityIds()).containsExactlyInAnyOrder(SecurityIds.VW_VZ);
    assertThat(position.getDisplayName()).isEqualTo("Volkswagen VZ");
    assertThat(position.getSecurityGroupId()).isEqualTo(1);
    assertThat(position.getCount()).isEqualTo(9);
    assertThat(position.getBuyInAbsolute()).isEqualTo(890.19);
    assertThat(position.getBuyInRelative()).isCloseTo(23.45852, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getCurrentSizeAbsolute()).isEqualTo(1006.2);
    assertThat(position.getCurrentSizeRelative()).isCloseTo(18.70462, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getAbsolutePerformance()).isEqualTo(116.01);
    assertThat(position.getRelativePerformance()).isCloseTo(13.03205, ACCURACY_ONE_THOUSANDTH);
  }

  @Test
  void getDepotPositions_consolidateGroupsAndDepots_ok() throws Exception {
    MvcResult result = getDepotPositions("7,8").andExpect(status().isOk()).andReturn();
    DepotCompositionDto responseBody = objectMapper.readValue(result.getResponse().getContentAsString(),
        DepotCompositionDto.class);
    assertThat(responseBody.getCurrency()).isEqualTo("EUR");
    assertThat(responseBody.getBuyInAbsolute()).isEqualTo(11043.48);
    assertThat(responseBody.getCurrentSizeAbsolute()).isCloseTo(12676.10, ACCURACY_ONE_HUNDREDTH);
    assertThat(responseBody.getAbsolutePerformance()).isCloseTo(1632.62, ACCURACY_ONE_HUNDREDTH);
    assertThat(responseBody.getRelativePerformance()).isCloseTo(14.78, ACCURACY_ONE_HUNDREDTH);

    List<DepotPositionDto> positions = responseBody.getPositions();
    assertThat(positions).isNotNull();
    assertThat(positions).hasSize(2);

    DepotPositionDto position = positions.getFirst();
    assertThat(position.getSecurityIds()).containsExactlyInAnyOrder(SecurityIds.GOOGL, SecurityIds.GOOG);
    assertThat(position.getDisplayName()).isEqualTo("Alphabet");
    assertThat(position.getSecurityGroupId()).isEqualTo(2);
    assertThat(position.getCount()).isEqualTo(67.766);
    assertThat(position.getBuyInAbsolute()).isEqualTo(7202.29);
    assertThat(position.getBuyInRelative()).isCloseTo(65.21758, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getCurrentSizeAbsolute()).isCloseTo(8601.42616, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getCurrentSizeRelative()).isCloseTo(67.85546, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getAbsolutePerformance()).isCloseTo(1399.13616, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getRelativePerformance()).isCloseTo(19.42627, ACCURACY_ONE_THOUSANDTH);

    position = positions.get(1);
    assertThat(position.getSecurityIds()).containsExactlyInAnyOrder(SecurityIds.VW_VZ, SecurityIds.VW_STAMM);
    assertThat(position.getDisplayName()).isEqualTo("Volkswagen");
    assertThat(position.getSecurityGroupId()).isEqualTo(1);
    assertThat(position.getCount()).isEqualTo(36);
    assertThat(position.getBuyInAbsolute()).isEqualTo(3841.19);
    assertThat(position.getBuyInRelative()).isCloseTo(34.78242, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getCurrentSizeAbsolute()).isEqualTo(4074.675);
    assertThat(position.getCurrentSizeRelative()).isCloseTo(32.14454, ACCURACY_ONE_THOUSANDTH);
    assertThat(position.getAbsolutePerformance()).isEqualTo(233.485);
    assertThat(position.getRelativePerformance()).isCloseTo(6.07845, ACCURACY_ONE_THOUSANDTH);
  }

  private ResultActions getDepotPositions(String depotIds) throws Exception {
    return mockMvc.perform(get(String.format(ENDPOINT, depotIds)));
  }
}
