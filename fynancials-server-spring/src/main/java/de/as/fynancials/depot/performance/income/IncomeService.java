package de.as.fynancials.depot.performance.income;

import de.as.fynancials.common.error.BadRequestException;
import de.as.fynancials.depot.performance.api.model.IncomeTypeDto;
import de.as.fynancials.depot.performance.model.Performance;
import java.util.List;
import java.util.Set;

public interface IncomeService {

  List<Performance> getIncome(Set<Long> depotIds, Set<Long> securityIds, Set<IncomeTypeDto> incomeTypes)
      throws BadRequestException;
}
