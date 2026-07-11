package de.as.fynancials.exchangerates;

import static integration.Accuracy.ACCURACY_ONE_THOUSANDTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import de.as.fynancials.common.error.NotFoundException;
import integration.IntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ExchangeRateServiceTest {

  @Autowired
  private ExchangeRateService subject;

  @Test
  void convertCurrency_directExchangeRate_sameDate() {
    BigDecimal eur = new BigDecimal("1000");
    BigDecimal expectedUsd = new BigDecimal("1077.1");
    BigDecimal usd = subject.convert(eur, "EUR", "USD", LocalDate.of(2023, Month.DECEMBER, 7));
    assertThat(usd).isEqualByComparingTo(expectedUsd);
  }

  @Test
  void convertCurrency_invertedExchangeRate_sameDay() {
    BigDecimal usd = new BigDecimal("1000");
    BigDecimal expectedEur = new BigDecimal("928.4189");
    BigDecimal eur = subject.convert(usd, "USD", "EUR", LocalDate.of(2023, Month.DECEMBER, 7));
    assertThat(eur).isCloseTo(expectedEur, ACCURACY_ONE_THOUSANDTH);
  }

  @Test
  void convertCurrency_directExchangeRate_daysBefore() {
    BigDecimal eur = new BigDecimal("1000");
    BigDecimal expectedUsd = new BigDecimal("1077.7");
    BigDecimal usd;

    for (int i = 9; i <= 15; i++) {
      usd = subject.convert(eur, "EUR", "USD", LocalDate.of(2023, Month.DECEMBER, i));
      assertThat(usd).isEqualByComparingTo(expectedUsd);
    }
  }

  @Test
  void convertCurrency_indirectExchangeRate_daysBefore() {
    BigDecimal usd = new BigDecimal("1000");
    BigDecimal expectedEur = new BigDecimal("927.90201");
    BigDecimal eur;

    for (int i = 9; i <= 15; i++) {
      eur = subject.convert(usd, "USD", "EUR", LocalDate.of(2023, Month.DECEMBER, i));
      assertThat(eur).isCloseTo(expectedEur, ACCURACY_ONE_THOUSANDTH);
    }
  }

  @Test
  void convertCurrency_directExchangeRate_outdated() {
    BigDecimal eur = new BigDecimal("1000");
    BigDecimal expectedUsd = new BigDecimal("1077.7");
    boolean caughtException = false;

    try {
      subject.convert(eur, "EUR", "USD", LocalDate.of(2023, Month.DECEMBER, 16));
    } catch (OutdatedExchangeRateException e) {
      caughtException = true;
      assertThat(e.getDate()).isEqualTo(LocalDate.of(2023, Month.DECEMBER, 8));
      assertThat(e.getBaseCurrency()).isEqualTo("EUR");
      assertThat(e.getTargetCurrency()).isEqualTo("USD");
      assertThat(e.getExchangeRate()).isEqualByComparingTo(new BigDecimal("1.0777"));
      assertThat(e.getConversionResult()).isEqualByComparingTo(expectedUsd);
    }

    assertThat(caughtException).isTrue();
  }

  @Test
  void convertCurrency_indirectExchangeRate_outdated() {
    BigDecimal usd = new BigDecimal("1000");
    BigDecimal expectedEur = new BigDecimal("927.90201");
    boolean caughtException = false;

    try {
      subject.convert(usd, "USD", "EUR", LocalDate.of(2023, Month.DECEMBER, 16));
    } catch (OutdatedExchangeRateException e) {
      caughtException = true;
      assertThat(e.getDate()).isEqualTo(LocalDate.of(2023, Month.DECEMBER, 8));
      // exception should contain base/target currency from the database entry that was actually used for calculation
      assertThat(e.getBaseCurrency()).isEqualTo("EUR");
      assertThat(e.getTargetCurrency()).isEqualTo("USD");
      assertThat(e.getExchangeRate()).isEqualByComparingTo(new BigDecimal("1.0777"));
      assertThat(e.getConversionResult()).isCloseTo(expectedEur, ACCURACY_ONE_THOUSANDTH);
    }

    assertThat(caughtException).isTrue();
  }

  @Test
  void convertCurrency_noPreviousExchangeRateAvailable() {
    assertThatExceptionOfType(NotFoundException.class).isThrownBy(
        () -> subject.convert(BigDecimal.ONE, "EUR", "USD", LocalDate.of(2023, Month.NOVEMBER, 28)));
  }

  @Test
  void convertCurrency_noExchangeRateAvailableAtAll() {
    assertThatExceptionOfType(NotFoundException.class).isThrownBy(
        () -> subject.convert(BigDecimal.ONE, "EUR", "XYZ", LocalDate.of(2023, Month.DECEMBER, 7)));
  }
}
