package de.as.fynancials.depot.dividend;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;

@Data
class Dividend {

  private List<Long> securityIds;
  private String displayName;
  private Long securityGroupId;
  private BigDecimal absoluteValueGross;
  private BigDecimal absoluteValueNet;
  private BigDecimal relativeValueGross;
  private BigDecimal relativeValueNet;

  Dividend() {
    securityIds = new LinkedList<>();
  }

  boolean isGrouped() {
    return securityIds.size() > 1;
  }
}
