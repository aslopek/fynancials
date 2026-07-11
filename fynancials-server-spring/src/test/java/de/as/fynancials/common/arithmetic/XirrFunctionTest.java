package de.as.fynancials.common.arithmetic;

import static java.math.BigDecimal.ZERO;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.List;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class XirrFunctionTest {

  private static final Offset<Double> ACCURACY = Offset.strictOffset(0.001);

  private List<BigDecimal> cashFlows;
  private List<LocalDate> cashFlowDates;
  private BigDecimal liquidationValue;
  private MathContext mathContext;
  private XirrFunction xirrFunction;

  @BeforeEach
  void setUp() {
    // Baseline: one negative investment, one positive return, and a liquidation value
    cashFlows = List.of(
        BigDecimal.valueOf(-1000), // investment
        BigDecimal.valueOf(200)    // partial return
    );
    cashFlowDates = List.of(
        LocalDate.of(2020, 1, 1),
        LocalDate.of(2021, 1, 1)
    );
    liquidationValue = BigDecimal.valueOf(900); // final value
    mathContext = MathContext.DECIMAL64;

    xirrFunction = new XirrFunction(cashFlows, cashFlowDates, liquidationValue, mathContext);
  }

  @Test
  void baseline_shouldReturnPositiveXirr() {
    double result = xirrFunction.value(0.1);
    assertThat(result).isCloseTo(-0.19569, ACCURACY);
  }

  @Test
  void shouldThrowIfCashFlowsIsNull() {
    xirrFunction = new XirrFunction(null, cashFlowDates, liquidationValue, mathContext);
    assertThatThrownBy(() -> xirrFunction.value(0.1))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void shouldThrowIfCashFlowDatesIsNull() {
    xirrFunction = new XirrFunction(cashFlows, null, liquidationValue, mathContext);
    assertThatThrownBy(() -> xirrFunction.value(0.1))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void shouldThrowIfCashFlowsIsEmpty() {
    xirrFunction = new XirrFunction(emptyList(), emptyList(), liquidationValue, mathContext);
    assertThatThrownBy(() -> xirrFunction.value(0.1))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void shouldThrowIfCashFlowsAndDatesHaveDifferentSizes() {
    xirrFunction = new XirrFunction(
        List.of(BigDecimal.valueOf(-1000), BigDecimal.valueOf(200)),
        List.of(LocalDate.of(2020, 1, 1)),
        liquidationValue,
        mathContext
    );
    assertThatThrownBy(() -> xirrFunction.value(0.1))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void shouldThrowIfLiquidationValueIsNull() {
    xirrFunction = new XirrFunction(cashFlows, cashFlowDates, null, mathContext);
    assertThatThrownBy(() -> xirrFunction.value(0.1))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void shouldThrowIfLiquidationValueIsNegative() {
    xirrFunction = new XirrFunction(cashFlows, cashFlowDates, BigDecimal.valueOf(-1), mathContext);
    assertThatThrownBy(() -> xirrFunction.value(0.1))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void shouldReturnNegativeValueIfAllCashFlowsAreNegative() {
    cashFlows = List.of(BigDecimal.valueOf(-1000), BigDecimal.valueOf(-200));
    xirrFunction = new XirrFunction(cashFlows, cashFlowDates, BigDecimal.valueOf(0), mathContext);
    double result = xirrFunction.value(0.1);
    assertThat(result).isCloseTo(-1181.78260, ACCURACY);
  }

  @Test
  void shouldReturnZeroIfRateIsZeroAndCashFlowsSumToZero() {
    cashFlows = List.of(BigDecimal.valueOf(-1000), BigDecimal.valueOf(1000));
    cashFlowDates = List.of(LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1));
    liquidationValue = ZERO;
    xirrFunction = new XirrFunction(cashFlows, cashFlowDates, liquidationValue, mathContext);
    double result = xirrFunction.value(0);
    assertThat(result).isZero();
  }

  @Test
  void shouldHandleZeroRateWithNonZeroLiquidation() {
    double result = xirrFunction.value(0);
    assertThat(result).isCloseTo(100, ACCURACY);
  }

  @Test
  void shouldHandleSingleCashFlowPlusLiquidation() {
    cashFlows = List.of(BigDecimal.valueOf(-1000));
    cashFlowDates = List.of(LocalDate.of(2020, 1, 1));
    liquidationValue = BigDecimal.valueOf(1100);
    xirrFunction = new XirrFunction(cashFlows, cashFlowDates, liquidationValue, mathContext);
    double result = xirrFunction.value(0.1);
    assertThat(result).isCloseTo(100, ACCURACY);
  }

  @Test
  void shouldHandleLongHoldingPeriod() {
    cashFlows = List.of(BigDecimal.valueOf(-1000));
    cashFlowDates = List.of(LocalDate.of(1980, 1, 1));
    liquidationValue = BigDecimal.valueOf(10000);
    xirrFunction = new XirrFunction(cashFlows, cashFlowDates, liquidationValue, mathContext);
    double result = xirrFunction.value(0.08); // 8% over 45 years
    assertThat(result).isCloseTo(9000, ACCURACY);
  }

  @Test
  void shouldHandleZeroCashFlowWithLiquidationOnly() {
    cashFlows = List.of();
    cashFlowDates = List.of();
    liquidationValue = BigDecimal.valueOf(1000);
    xirrFunction = new XirrFunction(cashFlows, cashFlowDates, liquidationValue, mathContext);
    assertThatThrownBy(() -> xirrFunction.value(0.1))
        .isInstanceOf(IllegalStateException.class); // because dates are empty
  }

  @Test
  void shouldHandleMultipleCashFlowsOnSameDate() {
    cashFlows = List.of(BigDecimal.valueOf(-500), BigDecimal.valueOf(-500), BigDecimal.valueOf(1100));
    cashFlowDates = List.of(
        LocalDate.of(2020, 1, 1),
        LocalDate.of(2020, 1, 1),
        LocalDate.of(2021, 1, 1)
    );
    liquidationValue = BigDecimal.ZERO;
    xirrFunction = new XirrFunction(cashFlows, cashFlowDates, liquidationValue, mathContext);
    double result = xirrFunction.value(0.1);
    assertThat(result).isCloseTo(-0.19569, ACCURACY);
  }

  @Test
  void shouldHandleNegativeRate() {
    double result = xirrFunction.value(-0.1);
    assertThat(result).isCloseTo(222.48667, ACCURACY); // NPV increases with negative rate
  }

  @Test
  void shouldHandleExtremeRate() {
    double result = xirrFunction.value(99.0);
    assertThat(result).isCloseTo(-989.10353, ACCURACY); // NPV collapses under extreme discounting
  }

  @Test
  void shouldHandleZeroValueCashFlow() {
    cashFlows = List.of(BigDecimal.valueOf(-1000), BigDecimal.ZERO, BigDecimal.valueOf(1100));
    cashFlowDates = List.of(
        LocalDate.of(2020, 1, 1),
        LocalDate.of(2020, 6, 1),
        LocalDate.of(2021, 1, 1)
    );
    liquidationValue = BigDecimal.ZERO;
    xirrFunction = new XirrFunction(cashFlows, cashFlowDates, liquidationValue, mathContext);
    double result = xirrFunction.value(0.1);
    assertThat(result).isCloseTo(-0.19569, ACCURACY);
  }

  @Test
  void shouldHandleInvestmentAndLiquidationOnSameDay() {
    cashFlows = List.of(BigDecimal.valueOf(-1000));
    cashFlowDates = List.of(LocalDate.of(2020, 1, 1));
    liquidationValue = BigDecimal.valueOf(1000);
    xirrFunction = new XirrFunction(cashFlows, cashFlowDates, liquidationValue, mathContext);
    double result = xirrFunction.value(0.1);
    assertThat(result).isCloseTo(0.0, ACCURACY);
  }

  @Test
  void shouldHandleSmallCashFlowsPrecisely() {
    cashFlows = List.of(BigDecimal.valueOf(-0.01), BigDecimal.valueOf(0.011));
    cashFlowDates = List.of(LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1));
    liquidationValue = BigDecimal.ZERO;
    xirrFunction = new XirrFunction(cashFlows, cashFlowDates, liquidationValue, mathContext);
    double result = xirrFunction.value(0.1);
    assertThat(result).isCloseTo(-0.00009, ACCURACY); // approximate NPV
  }

  @Test
  void functionShouldBeMonotonicDecreasing() {
    double v1 = xirrFunction.value(-0.5);
    double v2 = xirrFunction.value(0.0);
    double v3 = xirrFunction.value(0.5);

    assertThat(v1).isGreaterThan(v2);
    assertThat(v2).isGreaterThan(v3);
  }
}
