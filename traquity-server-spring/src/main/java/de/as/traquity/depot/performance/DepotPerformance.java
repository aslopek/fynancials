package de.as.traquity.depot.performance;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class DepotPerformance {

  private List<DepotValue> values;
  private BigDecimal extendedInternalRateOfReturn = ZERO;
}
