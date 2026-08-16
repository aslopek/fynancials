package de.as.traquity.price.security.historical;

import de.as.traquity.common.error.NotFoundException;
import de.as.traquity.price.security.historical.api.controller.HistoricalSecurityPriceConfigApiDelegate;
import de.as.traquity.price.security.historical.api.model.HistoricalSecurityPriceConfigCreateDto;
import de.as.traquity.price.security.historical.api.model.HistoricalSecurityPriceConfigReadDto;
import de.as.traquity.price.security.historical.api.model.HistoricalSecurityPriceConfigUpdateDto;
import de.as.traquity.security.SecurityService;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
class HistoricalPriceConfigController implements HistoricalSecurityPriceConfigApiDelegate {

  private static final String CONFIG_URI_PATTERN = "/securities/%d/historical-prices/config";

  private final HistoricalSecurityPriceServiceImpl historicalSecurityPriceService;
  private final HistoricalSecurityPriceConfigMapper historicalSecurityPriceConfigMapper;
  private final SecurityService securityService;

  @Override
  public ResponseEntity<HistoricalSecurityPriceConfigReadDto> createHistoricalSecurityPriceConfig(
      Long securityId, HistoricalSecurityPriceConfigCreateDto historicalSecurityPriceConfigCreateDto) {
    if (!securityService.securityExists(securityId)) {
      throw new NotFoundException();
    }

    HistoricalSecurityPriceConfig config =
        historicalSecurityPriceConfigMapper.fromCreateDto(historicalSecurityPriceConfigCreateDto);
    config.setSecurityId(securityId);
    config = historicalSecurityPriceService.createConfig(config);

    HistoricalSecurityPriceConfigReadDto responseBody = historicalSecurityPriceConfigMapper.toDto(config);
    URI locationHeader = URI.create(String.format(CONFIG_URI_PATTERN, securityId));
    return ResponseEntity.created(locationHeader).body(responseBody);
  }

  @Override
  public ResponseEntity<HistoricalSecurityPriceConfigReadDto> getHistoricalSecurityPriceConfig(Long securityId) {
    HistoricalSecurityPriceConfig config = historicalSecurityPriceService.getConfig(securityId);
    return ResponseEntity.ok(historicalSecurityPriceConfigMapper.toDto(config));
  }

  @Override
  public ResponseEntity<HistoricalSecurityPriceConfigReadDto> updateHistoricalSecurityPriceConfig(
      Long securityId, HistoricalSecurityPriceConfigUpdateDto historicalSecurityPriceConfigUpdateDto,
      Boolean removeExistingPrices) {
    HistoricalSecurityPriceConfig config =
        historicalSecurityPriceConfigMapper.fromUpdateDto(historicalSecurityPriceConfigUpdateDto);
    config.setSecurityId(securityId);
    config = historicalSecurityPriceService.updateConfig(config);

    HistoricalSecurityPriceConfigReadDto responseBody = historicalSecurityPriceConfigMapper.toDto(config);

    if (Boolean.TRUE.equals(removeExistingPrices)) {
      historicalSecurityPriceService.deletePrices(securityId);
      historicalSecurityPriceService.updatePrices(securityId);
    }

    return ResponseEntity.ok(responseBody);
  }
}
