package de.as.fynancials.common.arithmetic;

import static de.as.fynancials.common.arithmetic.MathFunctions.pow;
import static de.as.fynancials.common.config.TimeConfig.DAYS_PER_YEAR;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.analysis.UnivariateFunction;

@RequiredArgsConstructor
public class XirrFunction implements UnivariateFunction {

  private final List<BigDecimal> cashFlows;
  private final List<LocalDate> cashFlowDates;
  private final BigDecimal liquidationValue;
  private final MathContext mathContext;

  @Override
  public double value(double rate) {
    return value(BigDecimal.valueOf(rate)).doubleValue();
  }

  private BigDecimal value(BigDecimal rate) {
    if (cashFlows == null || cashFlowDates == null || cashFlows.isEmpty() || cashFlows.size() != cashFlowDates.size()
        || liquidationValue == null || liquidationValue.compareTo(ZERO) < 0) {
      throw new IllegalStateException();
    }

    final LocalDate baseDate = cashFlowDates.getFirst();
    long days;
    BigDecimal result = ZERO;
    BigDecimal years;
    BigDecimal discountFactor = ONE;
    BigDecimal discountedCashFlow;

    for (int i = 0; i < cashFlows.size(); i++) {
      days = ChronoUnit.DAYS.between(baseDate, cashFlowDates.get(i));
      years = new BigDecimal(days).divide(DAYS_PER_YEAR, mathContext);
      discountFactor = pow(ONE.add(rate, mathContext), years, mathContext);
      discountedCashFlow = cashFlows.get(i).divide(discountFactor, mathContext);
      result = result.add(discountedCashFlow, mathContext);
    }

    discountedCashFlow = liquidationValue.divide(discountFactor, mathContext);
    result = result.add(discountedCashFlow, mathContext);
    return result;
  }
}
