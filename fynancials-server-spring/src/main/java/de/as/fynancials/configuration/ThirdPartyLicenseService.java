package de.as.fynancials.configuration;

import de.as.fynancials.common.error.InternalServerErrorException;
import de.as.fynancials.config.api.model.ThirdPartyLicenseDto;
import java.util.List;

public interface ThirdPartyLicenseService {

  /**
   * Returns license information (name, version, license name, verbatim license/notice texts) for every third-party
   * library on the runtime classpath. When running from the packaged fat jar this is exactly the set of libraries
   * shipped to the user ({@code BOOT-INF/lib}), which makes the result suitable for license attribution.
   */
  List<ThirdPartyLicenseDto> getThirdPartyLicenses() throws InternalServerErrorException;
}
