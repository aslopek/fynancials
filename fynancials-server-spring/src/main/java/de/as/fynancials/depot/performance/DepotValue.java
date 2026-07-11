package de.as.fynancials.depot.performance;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class DepotValue {

  private final LocalDate date;
  private BigDecimal investedCapital = ZERO;
  private BigDecimal cashPosition = ZERO;
  private BigDecimal absoluteValue = ZERO;
  private BigDecimal performanceAbsolute = ZERO;
  private BigDecimal performanceRelative = ZERO;
  private BigDecimal xirr = ZERO;
  private Map<Long, BigDecimal> positions = new HashMap<>();
}
