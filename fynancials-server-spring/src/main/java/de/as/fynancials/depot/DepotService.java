package de.as.fynancials.depot;

import de.as.fynancials.common.error.BadRequestException;
import de.as.fynancials.common.error.ConflictException;
import de.as.fynancials.common.error.NoContentException;
import de.as.fynancials.common.error.NotFoundException;
import java.util.List;
import java.util.Set;
import org.springframework.core.io.Resource;

public interface DepotService {

  Depot createDepot(String name, String currency) throws BadRequestException, ConflictException;

  List<Depot> getDepots() throws NoContentException;

  boolean depotExists(long depotId);

  Depot getDepot(Long id) throws NotFoundException;

  Depot updateDepot(Long id, String name, String currency, Long version)
      throws BadRequestException, NotFoundException, ConflictException;

  void deleteDepot(Long id) throws NotFoundException;

  boolean depotsHaveSameCurrency(Set<Long> depotIds) throws NotFoundException;

  boolean hasLogo(Long depotId);

  Resource getLogo(Long depotId) throws NotFoundException;

  void setLogo(Long depotId, Resource logo) throws BadRequestException;

  void deleteLogo(Long depotId);
}
