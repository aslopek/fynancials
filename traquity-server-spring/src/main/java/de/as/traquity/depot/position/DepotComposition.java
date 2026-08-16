package de.as.traquity.depot.position;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class DepotComposition {

  private String currency;
  private BigDecimal buyInAbsolute;
  private BigDecimal currentSizeAbsolute;
  private BigDecimal performanceAbsolute;
  private BigDecimal performanceRelative;
  private List<DepotPosition> positions;
}
