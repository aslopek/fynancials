package de.as.fynancials.price.security.historical;

import de.as.fynancials.price.security.historical.api.controller.HistoricalSecurityPriceApiDelegate;
import de.as.fynancials.price.security.historical.api.model.HistoricalSecurityPriceConfigDto;
import de.as.fynancials.price.security.historical.api.model.HistoricalSecurityPriceDto;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
class HistoricalPriceController implements HistoricalSecurityPriceApiDelegate {

  private final HistoricalSecurityPriceServiceImpl historicalSecurityPriceService;
  private final HistoricalSecurityPriceMapper historicalSecurityPriceMapper;
  private final HistoricalSecurityPriceConfigMapper historicalSecurityPriceConfigMapper;


  @Override
  public ResponseEntity<List<HistoricalSecurityPriceDto>> getHistoricalPrices(Long securityId, LocalDate startDate,
                                                                              String currency) {
    List<HistoricalSecurityPrice> prices;
    if (currency == null) {
      prices = historicalSecurityPriceService.getPrices(securityId, startDate);
    } else {
      prices = historicalSecurityPriceService.getPrices(securityId, startDate, currency);
    }
    List<HistoricalSecurityPriceDto> responseBody = prices.stream().map(historicalSecurityPriceMapper::toDto).toList();
    return ResponseEntity.ok(responseBody);
  }

  @Override
  public ResponseEntity<HistoricalSecurityPriceConfigDto> getHistoricalPriceConfig(Long securityId) {
    HistoricalSecurityPriceConfig config = historicalSecurityPriceService.getConfig(securityId);
    HistoricalSecurityPriceConfigDto responseBody = historicalSecurityPriceConfigMapper.toDto(config);
    return ResponseEntity.ok(responseBody);
  }

  @Override
  public ResponseEntity<HistoricalSecurityPriceConfigDto> setHistoricalPriceConfig(Long securityId,
                                                                                   HistoricalSecurityPriceConfigDto historicalSecurityPriceConfigDto,
                                                                                   Boolean removeExistingPrices) {
    HistoricalSecurityPriceConfig config =
        historicalSecurityPriceConfigMapper.fromDto(historicalSecurityPriceConfigDto);
    config.setSecurityId(securityId);
    config = historicalSecurityPriceService.setConfig(config);
    HistoricalSecurityPriceConfigDto responseBody = historicalSecurityPriceConfigMapper.toDto(config);

    if (Boolean.TRUE.equals(removeExistingPrices)) {
      historicalSecurityPriceService.deletePrices(securityId);
      historicalSecurityPriceService.updatePrices(securityId);
    }

    return ResponseEntity.ok(responseBody);
  }
}
