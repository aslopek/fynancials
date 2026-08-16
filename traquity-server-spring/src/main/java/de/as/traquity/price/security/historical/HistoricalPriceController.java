package de.as.traquity.price.security.historical;

import de.as.traquity.common.error.NotFoundException;
import de.as.traquity.price.security.historical.api.controller.HistoricalSecurityPriceApiDelegate;
import de.as.traquity.price.security.historical.api.model.HistoricalSecurityPriceDto;
import de.as.traquity.security.SecurityService;
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
  private final SecurityService securityService;

  @Override
  public ResponseEntity<List<HistoricalSecurityPriceDto>> getHistoricalSecurityPrices(Long securityId,
                                                                                      LocalDate startDate,
                                                                                      String currency) {
    if (!securityService.securityExists(securityId)) {
      throw new NotFoundException();
    }
    List<HistoricalSecurityPrice> prices;
    if (currency == null) {
      prices = historicalSecurityPriceService.getPrices(securityId, startDate);
    } else {
      prices = historicalSecurityPriceService.getPrices(securityId, startDate, currency);
    }
    List<HistoricalSecurityPriceDto> responseBody = prices.stream().map(historicalSecurityPriceMapper::toDto).toList();
    return ResponseEntity.ok(responseBody);
  }
}
