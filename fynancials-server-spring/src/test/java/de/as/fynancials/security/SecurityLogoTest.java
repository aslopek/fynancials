package de.as.fynancials.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import integration.IntegrationTest;
import integration.SecurityIds;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@IntegrationTest
class SecurityLogoTest {

  private static final String ENDPOINT = "/securities/%d/logo";
  private static byte[] greenLogo;
  private static byte[] blueLargeLogo;
  private static byte[] blueLargeLogoScaled;
  private static byte[] blueLogo;

  /**
   * Logo of {@link SecurityIds#MAIN} as defined in the test data.
   */
  private static byte[] mainLogo;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private SecurityLogoRepository securityLogoRepository;

  @BeforeAll
  static void beforeAll() {
    try (InputStream inputStream = SecurityLogoTest.class.getClassLoader()
        .getResourceAsStream("db/example-data/green.png")) {
      if (inputStream == null) {
        throw new IOException();
      }
      greenLogo = inputStream.readAllBytes();
    } catch (IOException e) {
      fail(e.getMessage());
    }

    try (InputStream inputStream = SecurityLogoTest.class.getClassLoader()
        .getResourceAsStream("logos/blue-large.png")) {
      if (inputStream == null) {
        throw new IOException();
      }
      blueLargeLogo = inputStream.readAllBytes();
    } catch (IOException e) {
      fail(e.getMessage());
    }

    try (InputStream inputStream = SecurityLogoTest.class.getClassLoader()
        .getResourceAsStream("logos/blue-large-scaled.png")) {
      if (inputStream == null) {
        throw new IOException();
      }
      blueLargeLogoScaled = inputStream.readAllBytes();
    } catch (IOException e) {
      fail(e.getMessage());
    }

    try (InputStream inputStream = SecurityLogoTest.class.getClassLoader()
        .getResourceAsStream("db/example-data/blue.png")) {
      if (inputStream == null) {
        throw new IOException();
      }
      blueLogo = inputStream.readAllBytes();
    } catch (IOException e) {
      fail(e.getMessage());
    }

    try (InputStream inputStream = SecurityLogoTest.class.getClassLoader()
        .getResourceAsStream("db/example-data/blue.png")) {
      if (inputStream == null) {
        throw new IOException();
      }
      mainLogo = inputStream.readAllBytes();
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  void getLogo_ok() throws Exception {
    final long securityId = SecurityIds.MAIN;
    MvcResult mvcResult =
        mockMvc.perform(get(String.format(ENDPOINT, securityId))).andExpect(status().isOk()).andReturn();
    assertThat(mvcResult.getResponse().getContentType()).isEqualTo(MediaType.IMAGE_PNG_VALUE);

    byte[] fromDb = securityLogoRepository.findById(securityId).orElseThrow().getLogo();
    byte[] fromResponse = mvcResult.getResponse().getContentAsByteArray();
    assertThat(fromResponse).isEqualTo(fromDb);
    assertThat(fromResponse).isEqualTo(mainLogo);
  }

  @Test
  void getLogo_notFound() throws Exception {
    final long securityId = SecurityIds.WTI_CRUDE_OIL_CERTIFICATE;
    MvcResult mvcResult =
        mockMvc.perform(get(String.format(ENDPOINT, securityId))).andExpect(status().isNotFound()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();
  }

  @Test
  void putLogo_replaceExisting_noScaling() throws Exception {
    final long securityId = SecurityIds.MAIN;
    // precondition
    assertThat(securityLogoRepository.existsById(securityId)).isTrue();

    MvcResult mvcResult =
        mockMvc.perform(put(String.format(ENDPOINT, securityId)).contentType(MediaType.IMAGE_PNG).content(greenLogo))
            .andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();

    byte[] fromDb = securityLogoRepository.findById(securityId).orElseThrow().getLogo();
    assertThat(fromDb).isEqualTo(greenLogo);
  }

  @Test
  void putLogo_replaceExisting_scaleDown() throws Exception {
    final long securityId = SecurityIds.MAIN;
    // precondition
    assertThat(securityLogoRepository.existsById(securityId)).isTrue();

    MvcResult mvcResult =
        mockMvc.perform(put(String.format(ENDPOINT, securityId)).contentType(MediaType.IMAGE_PNG)
            .content(blueLargeLogo)).andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();

    byte[] fromDb = securityLogoRepository.findById(securityId).orElseThrow().getLogo();
    assertThat(fromDb).isEqualTo(blueLargeLogoScaled);
  }

  @Test
  void putLogo_addNew_noScaling() throws Exception {
    final long securityId = SecurityIds.WTI_CRUDE_OIL_CERTIFICATE;
    // precondition
    assertThat(securityLogoRepository.existsById(securityId)).isFalse();

    MvcResult mvcResult =
        mockMvc.perform(put(String.format(ENDPOINT, securityId)).contentType(MediaType.IMAGE_PNG).content(blueLogo))
            .andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();

    byte[] fromDb = securityLogoRepository.findById(securityId).orElseThrow().getLogo();
    assertThat(fromDb).isEqualTo(blueLogo);
  }

  @Test
  void putLogo_addNew_scaleDown() throws Exception {
    final long securityId = SecurityIds.WTI_CRUDE_OIL_CERTIFICATE;
    // precondition
    assertThat(securityLogoRepository.existsById(securityId)).isFalse();

    MvcResult mvcResult =
        mockMvc.perform(put(String.format(ENDPOINT, securityId)).contentType(MediaType.IMAGE_PNG)
            .content(blueLargeLogo)).andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();

    byte[] fromDb = securityLogoRepository.findById(securityId).orElseThrow().getLogo();
    assertThat(fromDb).isEqualTo(blueLargeLogoScaled);
  }

  @Test
  void putLogo_securityDoesNotExist_badRequest() throws Exception {
    final long securityId = 999;
    final long count = securityLogoRepository.count();

    MvcResult mvcResult =
        mockMvc.perform(put(String.format(ENDPOINT, securityId)).contentType(MediaType.IMAGE_PNG).content(mainLogo))
            .andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();

    assertThat(securityLogoRepository.count()).isEqualTo(count);
    assertThat(securityLogoRepository.existsById(securityId)).isFalse();
  }

  @Test
  void putLogo_magicBytesAreWrong_badRequest() throws Exception {
    final long securityId = SecurityIds.WTI_CRUDE_OIL_CERTIFICATE;
    final long count = securityLogoRepository.count();

    byte[] requestBody = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09};
    MvcResult mvcResult =
        mockMvc.perform(put(String.format(ENDPOINT, securityId)).contentType(MediaType.IMAGE_PNG).content(requestBody))
            .andExpect(status().isBadRequest()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();

    assertThat(securityLogoRepository.count()).isEqualTo(count);
    assertThat(securityLogoRepository.existsById(securityId)).isFalse();
  }

  @Test
  void deleteLogo_exists_ok() throws Exception {
    final long securityId = SecurityIds.MAIN;
    // precondition
    assertThat(securityLogoRepository.existsById(securityId)).isTrue();

    final long count = securityLogoRepository.count();

    MvcResult mvcResult =
        mockMvc.perform(delete(String.format(ENDPOINT, securityId))).andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();

    assertThat(securityLogoRepository.count()).isEqualTo(count - 1);
    assertThat(securityLogoRepository.existsById(securityId)).isFalse();
  }

  @Test
  void deleteLogo_doesNotExist_ok() throws Exception {
    final long securityId = SecurityIds.WTI_CRUDE_OIL_CERTIFICATE;
    // precondition
    assertThat(securityLogoRepository.existsById(securityId)).isFalse();

    final long count = securityLogoRepository.count();

    MvcResult mvcResult =
        mockMvc.perform(delete(String.format(ENDPOINT, securityId))).andExpect(status().isNoContent()).andReturn();
    assertThat(mvcResult.getResponse().getContentLength()).isZero();

    assertThat(securityLogoRepository.count()).isEqualTo(count);
  }
}

