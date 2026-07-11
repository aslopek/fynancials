package de.as.fynancials.depot.dividend;

import de.as.fynancials.depot.dividend.api.controller.DividendApiDelegate;
import de.as.fynancials.depot.dividend.api.model.DividendsDto;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
class DividendController implements DividendApiDelegate {

  private final DividendService dividendService;

  @Override
  public ResponseEntity<DividendsDto> getDividends(List<Long> depots, Boolean includeSpecialDividends) {
    DividendsDto dividends = dividendService.getDividends(Set.copyOf(depots),
        includeSpecialDividends != null && includeSpecialDividends);
    return ResponseEntity.ok(dividends);
  }
}
