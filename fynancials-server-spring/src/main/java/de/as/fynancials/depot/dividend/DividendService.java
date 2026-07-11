package de.as.fynancials.depot.dividend;

import de.as.fynancials.common.error.BadRequestException;
import de.as.fynancials.depot.dividend.api.model.DividendsDto;
import java.util.Set;

public interface DividendService {

  DividendsDto getDividends(Set<Long> depotIds, boolean includeSpecialDividends) throws BadRequestException;
}
