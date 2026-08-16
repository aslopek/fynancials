package de.as.traquity.depot.position;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class DepotPosition implements Comparable<DepotPosition>, Performance {

  private List<Long> securityIds;
  private String displayName;
  private Long securityGroupId;
  private BigDecimal count;
  private BigDecimal buyInAbsolute;
  private BigDecimal buyInRelative;
  private BigDecimal currentSizeAbsolute;
  private BigDecimal currentSizeRelative;
  private BigDecimal performanceAbsolute;
  private BigDecimal performanceRelative;

  DepotPosition() {
    count = ZERO;
    buyInAbsolute = ZERO;
    buyInRelative = ZERO;
    currentSizeAbsolute = ZERO;
    currentSizeRelative = ZERO;
    performanceAbsolute = ZERO;
    performanceRelative = ZERO;
  }

  @Override
  public int compareTo(DepotPosition other) {
    return this.getCurrentSizeAbsolute().compareTo(other.getCurrentSizeAbsolute());
  }

  public Long getPositionId() {
    if (this.securityIds.size() == 1) {
      return securityIds.getFirst();
    } else {
      return securityGroupId;
    }
  }
}
