package de.as.fynancials.depot.position;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class DepotComposition {

  private String currency;
  private BigDecimal buyInAbsolute;
  private BigDecimal currentSizeAbsolute;
  private BigDecimal absolutePerformance;
  private BigDecimal relativePerformance;
  private List<DepotPosition> positions;
}
