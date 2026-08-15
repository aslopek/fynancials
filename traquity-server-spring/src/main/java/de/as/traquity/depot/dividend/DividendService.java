package de.as.traquity.depot.dividend;

import de.as.traquity.common.error.BadRequestException;
import de.as.traquity.depot.dividend.api.model.DividendsDto;
import java.util.Set;

public interface DividendService {

  DividendsDto getDividends(Set<Long> depotIds, boolean includeSpecialDividends) throws BadRequestException;
}
