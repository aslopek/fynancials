package de.as.traquity.depot.position;

import java.math.BigDecimal;

interface Performance {

  BigDecimal getCount();

  void setCount(BigDecimal count);

  BigDecimal getBuyInAbsolute();

  void setBuyInAbsolute(BigDecimal buyInAbsolute);

  BigDecimal getCurrentSizeAbsolute();

  void setCurrentSizeAbsolute(BigDecimal currentSizeAbsolute);

  BigDecimal getPerformanceAbsolute();

  void setPerformanceAbsolute(BigDecimal performanceAbsolute);

  BigDecimal getPerformanceRelative();

  void setPerformanceRelative(BigDecimal performanceRelative);
}
