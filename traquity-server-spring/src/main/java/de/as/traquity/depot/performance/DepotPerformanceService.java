package de.as.traquity.depot.performance;

import de.as.traquity.common.error.BadRequestException;
import de.as.traquity.common.error.NotFoundException;
import java.util.Set;

public interface DepotPerformanceService {

  DepotPerformance getDepotPerformance(Set<Long> depotIds) throws BadRequestException, NotFoundException;
}
