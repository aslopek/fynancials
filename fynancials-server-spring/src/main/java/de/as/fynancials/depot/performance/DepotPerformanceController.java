package de.as.fynancials.depot.performance;

import de.as.fynancials.depot.performance.api.controller.DepotPerformanceApiDelegate;
import de.as.fynancials.depot.performance.api.model.DepotPerformanceDto;
import jakarta.validation.constraints.Min;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
class DepotPerformanceController implements DepotPerformanceApiDelegate {

  private final DepotPerformanceService depotPerformanceService;
  private final DepotPerformanceMapper depotPerformanceMapper;

  @Override
  public ResponseEntity<DepotPerformanceDto> getDepotPerformance(List<@Min(1L) Long> depotIds) {
    DepotPerformance depotPerformance = depotPerformanceService.getDepotPerformance(new HashSet<>(depotIds));
    DepotPerformanceDto responseBody = depotPerformanceMapper.toDepotPerformanceDto(depotPerformance);
    return ResponseEntity.ok(responseBody);
  }
}
