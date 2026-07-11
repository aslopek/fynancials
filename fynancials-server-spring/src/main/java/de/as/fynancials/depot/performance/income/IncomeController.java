package de.as.fynancials.depot.performance.income;

import de.as.fynancials.common.error.NoContentException;
import de.as.fynancials.depot.performance.api.controller.DepotPerformanceIncomeApiDelegate;
import de.as.fynancials.depot.performance.api.model.IncomeTypeDto;
import de.as.fynancials.depot.performance.api.model.PerformanceDto;
import de.as.fynancials.depot.performance.mapper.PerformanceMapper;
import de.as.fynancials.depot.performance.model.Performance;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
class IncomeController implements DepotPerformanceIncomeApiDelegate {

  private final IncomeService incomeService;
  private final PerformanceMapper incomeMapper;

  @Override
  public ResponseEntity<List<PerformanceDto>> getIncome(List<Long> depots, List<Long> securities,
                                                        List<IncomeTypeDto> incomeTypes) {
    List<Performance> income =
        incomeService.getIncome(Set.copyOf(depots), Set.copyOf(securities), Set.copyOf(incomeTypes));
    if (income.isEmpty()) {
      throw new NoContentException();
    }
    List<PerformanceDto> responseBody = income.stream().map(incomeMapper::toPerformanceDto).toList();
    return ResponseEntity.ok(responseBody);
  }
}
