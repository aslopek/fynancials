package de.as.fynancials.depot.position;

import java.math.BigDecimal;

interface Performance {

  BigDecimal getCount();

  void setCount(BigDecimal count);

  BigDecimal getBuyInAbsolute();

  void setBuyInAbsolute(BigDecimal buyInAbsolute);

  BigDecimal getCurrentSizeAbsolute();

  void setCurrentSizeAbsolute(BigDecimal currentSizeAbsolute);

  BigDecimal getAbsolutePerformance();

  void setAbsolutePerformance(BigDecimal absolutePerformance);

  BigDecimal getRelativePerformance();

  void setRelativePerformance(BigDecimal relativePerformance);
}
