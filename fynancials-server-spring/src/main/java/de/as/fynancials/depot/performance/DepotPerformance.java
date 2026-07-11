package de.as.fynancials.depot.performance;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class DepotPerformance {

  private List<DepotValue> values;
  private BigDecimal extendedInternalRateOfReturns = ZERO;
}
