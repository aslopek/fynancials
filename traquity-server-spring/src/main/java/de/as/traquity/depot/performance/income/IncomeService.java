package de.as.traquity.depot.performance.income;

import de.as.traquity.common.error.BadRequestException;
import de.as.traquity.common.error.NotFoundException;
import de.as.traquity.depot.performance.api.model.IncomeTypeDto;
import de.as.traquity.depot.performance.model.Performance;
import java.util.List;
import java.util.Set;

public interface IncomeService {

  List<Performance> getIncome(Set<Long> depotIds, Set<Long> securityIds, Set<IncomeTypeDto> incomeTypes)
      throws BadRequestException, NotFoundException;
}
