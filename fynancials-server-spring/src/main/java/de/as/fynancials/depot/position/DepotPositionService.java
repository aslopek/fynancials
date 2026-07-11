package de.as.fynancials.depot.position;

import de.as.fynancials.common.error.BadRequestException;
import de.as.fynancials.common.error.NotFoundException;
import java.util.List;
import java.util.Set;

public interface DepotPositionService {

  DepotComposition getDepotPositions(Set<Long> depotIds, boolean consolidateSecurityGroups)
      throws BadRequestException;

  DepotComposition getDepotPositions(Long depotId, boolean consolidateSecurityGroups) throws BadRequestException;

  List<Lot> getLots(long depotId, long securityId) throws NotFoundException;
}
