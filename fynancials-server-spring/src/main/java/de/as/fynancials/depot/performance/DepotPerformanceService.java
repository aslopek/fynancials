package de.as.fynancials.depot.performance;

import de.as.fynancials.common.error.BadRequestException;
import de.as.fynancials.common.error.NotFoundException;
import java.util.Set;

public interface DepotPerformanceService {

  DepotPerformance getDepotPerformance(Set<Long> depotIds) throws BadRequestException, NotFoundException;
}
