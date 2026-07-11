package de.as.fynancials.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.as.fynancials.config.api.model.ThirdPartyLicenseDto;
import integration.IntegrationTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@IntegrationTest
class GetThirdPartyLicensesTest {

  private static final String ENDPOINT = "/config/third-party-licenses";

  @Autowired
  private MockMvc mockMvc;

  private ObjectMapper objectMapper;

  @BeforeEach
  void beforeEach() {
    objectMapper = new ObjectMapper().findAndRegisterModules();
  }

  @Test
  void getThirdPartyLicenses_ok() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get(ENDPOINT)).andExpect(status().isOk()).andReturn();
    List<ThirdPartyLicenseDto> licenses =
        objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

    assertThat(licenses).isNotEmpty();

    // every entry has the spec-required fields
    assertThat(licenses).allSatisfy(license -> {
      assertThat(license.getName()).isNotBlank();
      assertThat(license.getVersion()).isNotBlank();
    });

    // no own artifacts are reported as third-party
    assertThat(licenses).noneSatisfy(license -> assertThat(license.getName()).startsWith("fynancials"));

    // the list is sorted by name (case-insensitive)
    List<String> names = licenses.stream().map(license -> license.getName().toLowerCase()).toList();
    assertThat(names).isSorted();

    // well-known runtime dependencies are detected with their embedded license texts
    ThirdPartyLicenseDto jacksonCore = findByName(licenses, "jackson-core");
    assertThat(jacksonCore.getLicense()).containsIgnoringCase("apache");
    assertThat(jacksonCore.getLicenseText()).contains("Apache License");
    assertThat(jacksonCore.getNoticeText()).contains("Jackson");

    ThirdPartyLicenseDto commonsMath = findByName(licenses, "commons-math3");
    assertThat(commonsMath.getLicense()).containsIgnoringCase("apache");
    assertThat(commonsMath.getLicenseText()).contains("Apache License");

    ThirdPartyLicenseDto h2 = findByName(licenses, "h2");
    assertThat(h2.getVersion()).matches("\\d+\\..*");

    // libraries that don't embed their own LICENSE file fall back to the bundled canonical text (see
    // ThirdPartyLicenseServiceImpl.KNOWN_LICENSE_FALLBACKS)
    assertThat(h2.getLicense()).isEqualTo("Eclipse Public License 1.0");
    assertThat(h2.getLicenseText()).contains("Eclipse Public License - v 1.0");

    ThirdPartyLicenseDto liquibaseCore = findByName(licenses, "liquibase-core");
    assertThat(liquibaseCore.getLicense()).isEqualTo("Functional Source License 1.1 (ALv2 Future License)");
    assertThat(liquibaseCore.getLicenseText()).contains("Functional Source License, Version 1.1, ALv2 Future License");

    ThirdPartyLicenseDto asm = findByName(licenses, "asm");
    assertThat(asm.getLicense()).isEqualTo("BSD 3-Clause License");
    assertThat(asm.getLicenseText()).contains("Redistribution and use in source and binary forms");

    // aspectjweaver bundles a combined, non-standard-named license file (LICENSE-AspectJ.adoc) covering three
    // licenses at once (EPL-2.0 for the weaver itself, Apache-1.1/BSD-3-Clause for embedded third-party code) -
    // not picked up by LICENSE_FILE_PATTERN, so it falls back to the bundled combined text
    ThirdPartyLicenseDto aspectjweaver = findByName(licenses, "aspectjweaver");
    assertThat(aspectjweaver.getLicense()).isEqualTo("Eclipse Public License 2.0 AND BSD 3-Clause License AND Apache License 1.1");
    assertThat(aspectjweaver.getLicenseText()).contains("SPDX-License-Identifier: EPL-2.0 AND BSD-3-Clause AND Apache-1.1");
  }

  private ThirdPartyLicenseDto findByName(List<ThirdPartyLicenseDto> licenses, String name) {
    return licenses.stream().filter(license -> name.equals(license.getName())).findFirst().orElseThrow();
  }
}
