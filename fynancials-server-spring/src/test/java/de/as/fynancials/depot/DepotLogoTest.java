package de.as.fynancials.depot;

import integration.DepotIds;
import integration.IntegrationTest;
import java.io.IOException;
import java.io.InputStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class DepotLogoTest {

  private static final String ENDPOINT = "/depots/%d/logo";
  private static byte[] greenLogo;
  private static byte[] blueLargeLogo;
  private static byte[] blueLargeLogoScaled;
  private static byte[] depot1Logo;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private DepotLogoRepository depotLogoRepository;

  @BeforeAll
  static void beforeAll() {
    try (InputStream inputStream = DepotLogoTest.class.getClassLoader()
        .getResourceAsStream("db/example-data/green.png")) {
      if (inputStream == null) {
        throw new IOException();
      }
      greenLogo = inputStream.readAllBytes();
    } catch (IOException e) {
      fail(e.getMessage());
    }

    try (InputStream inputStream = DepotLogoTest.class.getClassLoader()
        .getResourceAsStream("logos/blue-large.png")) {
      if (inputStream == null) {
        throw new IOException();
      }
      blueLargeLogo = inputStream.readAllBytes();
    } catch (IOException e) {
      fail(e.getMessage());
    }

    try (InputStream inputStream = DepotLogoTest.class.getClassLoader()
        .getResourceAsStream("logos/blue-large-scaled.png")) {
      if (inputStream == null) {
        throw new IOException();
      }
      blueLargeLogoScaled = inputStream.readAllBytes();
    } catch (IOException e) {
      fail(e.getMessage());
    }

    try (InputStream inputStream = DepotLogoTest.class.getClassLoader()
        .getResourceAsStream("db/example-data/blue.png")) {
      if (inputStream == null) {
        throw new IOException();
      }
      depot1Logo = inputStream.readAllBytes();
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void getLogo_ok() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get(String.format(ENDPOINT, DepotIds.FIRST_DEPOT)))
        .andExpect(status().isOk()).andReturn();
    assertThat(mvcResult.getResponse().getContentType()).isEqualTo("image/png");

    byte[] fromDb = depotLogoRepository.findById(1L).orElseThrow().getLogo();
    byte[] fromResponse = mvcResult.getResponse().getContentAsByteArray();
    assertThat(fromResponse).isEqualTo(fromDb);
    assertThat(fromResponse).isEqualTo(depot1Logo);
  }

  @Test
  void getLogo_notFound() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get(String.format(ENDPOINT, 2))).andExpect(status().isNotFound()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void putLogo_replaceExisting_noScaling() throws Exception {
    final long depotId = 1;
    // precondition
    assertThat(depotLogoRepository.existsById(depotId)).isTrue();

    MvcResult mvcResult = putLogo(depotId, depot1Logo).andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();

    byte[] fromDb = depotLogoRepository.findById(depotId).orElseThrow().getLogo();
    assertThat(fromDb).isEqualTo(depot1Logo);
  }

  @Test
  void putLogo_replaceExisting_scaleDown() throws Exception {
    final long depotId = DepotIds.FIRST_DEPOT;
    // precondition
    assertThat(depotLogoRepository.existsById(depotId)).isTrue();

    MvcResult mvcResult = putLogo(depotId, blueLargeLogo).andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();

    byte[] fromDb = depotLogoRepository.findById(depotId).orElseThrow().getLogo();
    assertThat(fromDb).isEqualTo(blueLargeLogoScaled);
  }

  @Test
  void putLogo_addNew_noScaling() throws Exception {
    final long depotId = DepotIds.OTHER_DEPOT;
    // precondition
    assertThat(depotLogoRepository.existsById(depotId)).isFalse();

    MvcResult mvcResult = putLogo(depotId, greenLogo).andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();

    byte[] fromDb = depotLogoRepository.findById(depotId).orElseThrow().getLogo();
    assertThat(fromDb).isEqualTo(greenLogo);
  }

  @Test
  void putLogo_addNew_scaleDown() throws Exception {
    final long depotId = DepotIds.OTHER_DEPOT;
    // precondition
    assertThat(depotLogoRepository.existsById(depotId)).isFalse();

    MvcResult mvcResult = putLogo(depotId, blueLargeLogo).andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();

    byte[] fromDb = depotLogoRepository.findById(depotId).orElseThrow().getLogo();
    assertThat(fromDb).isEqualTo(blueLargeLogoScaled);
  }

  @Test
  void putLogo_depotDoesNotExist_badRequest() throws Exception {
    final long depotId = 999;
    final long count = depotLogoRepository.count();

    MvcResult mvcResult = putLogo(depotId, depot1Logo).andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();

    assertThat(depotLogoRepository.count()).isEqualTo(count);
    assertThat(depotLogoRepository.existsById(depotId)).isFalse();
  }

  @Test
  void putLogo_magicBytesAreWrong_badRequest() throws Exception {
    final long depotId = DepotIds.OTHER_DEPOT;
    final long count = depotLogoRepository.count();

    byte[] requestBody = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09};
    MvcResult mvcResult = putLogo(depotId, requestBody).andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();

    assertThat(depotLogoRepository.count()).isEqualTo(count);
    assertThat(depotLogoRepository.existsById(depotId)).isFalse();
  }

  @Test
  void deleteLogo_exists_ok() throws Exception {
    final long depotId = DepotIds.FIRST_DEPOT;
    //  precondition
    assertThat(depotLogoRepository.existsById(depotId)).isTrue();

    final long count = depotLogoRepository.count();

    MvcResult mvcResult =
        mockMvc.perform(delete(String.format(ENDPOINT, depotId))).andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();

    assertThat(depotLogoRepository.count()).isEqualTo(count - 1);
    assertThat(depotLogoRepository.existsById(depotId)).isFalse();
  }

  @Test
  void deleteLogo_doesNotExist_ok() throws Exception {
    final long depotId = DepotIds.OTHER_DEPOT;
    //  precondition
    assertThat(depotLogoRepository.existsById(depotId)).isFalse();

    final long count = depotLogoRepository.count();

    MvcResult mvcResult =
        mockMvc.perform(delete(String.format(ENDPOINT, depotId))).andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();

    assertThat(depotLogoRepository.count()).isEqualTo(count);
  }

  private ResultActions putLogo(long depotId, byte[] logoBytes) throws Exception {
    return mockMvc.perform(put(String.format(ENDPOINT, depotId)).contentType(MediaType.IMAGE_PNG).content(logoBytes));
  }
}
