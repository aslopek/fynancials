package de.as.fynancials.depot.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import de.as.fynancials.depot.transaction.api.model.TransactionTypeDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionTest {

  private Transaction subject;

  @BeforeEach
  void beforeEach() {
    subject = new Transaction();
    subject.setId(1L);
    subject.setVersion(0L);
    subject.setDate(LocalDate.of(2022, Month.JANUARY, 18));
    subject.setTime(LocalTime.of(14, 52, 20));
    subject.setDepotId(3L);
    subject.setSecurityId(48L);
    subject.setTransactionType(TransactionTypeDto.BUY);
    subject.setSecurityCountOriginal(new BigDecimal("3.804"));
    subject.setSecurityCountSplitAdjusted(new BigDecimal("11.412"));
    subject.setGrossValue(new BigDecimal("466.03"));
    subject.setTax(null);
    subject.setFee(new BigDecimal("0.99"));
  }

  @Test
  void calculateDateComponents_january() {
    subject.setDate(LocalDate.of(2022, Month.JANUARY, 18));
    assertThat(subject.getMonth()).isEqualTo(1);
    assertThat(subject.getQuarter()).isEqualTo(1);
  }

  @Test
  void calculateDateComponents_february() {
    subject.setDate(LocalDate.of(2022, Month.FEBRUARY, 18));
    assertThat(subject.getMonth()).isEqualTo(2);
    assertThat(subject.getQuarter()).isEqualTo(1);
  }

  @Test
  void calculateDateComponents_march() {
    subject.setDate(LocalDate.of(2022, Month.MARCH, 18));
    assertThat(subject.getMonth()).isEqualTo(3);
    assertThat(subject.getQuarter()).isEqualTo(1);
  }

  @Test
  void calculateDateComponents_april() {
    subject.setDate(LocalDate.of(2022, Month.APRIL, 18));
    assertThat(subject.getMonth()).isEqualTo(4);
    assertThat(subject.getQuarter()).isEqualTo(2);
  }

  @Test
  void calculateDateComponents_may() {
    subject.setDate(LocalDate.of(2022, Month.MAY, 18));
    assertThat(subject.getMonth()).isEqualTo(5);
    assertThat(subject.getQuarter()).isEqualTo(2);
  }

  @Test
  void calculateDateComponents_june() {
    subject.setDate(LocalDate.of(2022, Month.JUNE, 18));
    assertThat(subject.getMonth()).isEqualTo(6);
    assertThat(subject.getQuarter()).isEqualTo(2);
  }

  @Test
  void calculateDateComponents_july() {
    subject.setDate(LocalDate.of(2022, Month.JULY, 18));
    assertThat(subject.getMonth()).isEqualTo(7);
    assertThat(subject.getQuarter()).isEqualTo(3);
  }

  @Test
  void calculateDateComponents_august() {
    subject.setDate(LocalDate.of(2022, Month.AUGUST, 18));
    assertThat(subject.getMonth()).isEqualTo(8);
    assertThat(subject.getQuarter()).isEqualTo(3);
  }

  @Test
  void calculateDateComponents_september() {
    subject.setDate(LocalDate.of(2022, Month.SEPTEMBER, 18));
    assertThat(subject.getMonth()).isEqualTo(9);
    assertThat(subject.getQuarter()).isEqualTo(3);
  }

  @Test
  void calculateDateComponents_october() {
    subject.setDate(LocalDate.of(2022, Month.OCTOBER, 18));
    assertThat(subject.getMonth()).isEqualTo(10);
    assertThat(subject.getQuarter()).isEqualTo(4);
  }

  @Test
  void calculateDateComponents_november() {
    subject.setDate(LocalDate.of(2022, Month.NOVEMBER, 18));
    assertThat(subject.getMonth()).isEqualTo(11);
    assertThat(subject.getQuarter()).isEqualTo(4);
  }

  @Test
  void calculateDateComponents_december() {
    subject.setDate(LocalDate.of(2022, Month.DECEMBER, 18));
    assertThat(subject.getMonth()).isEqualTo(12);
    assertThat(subject.getQuarter()).isEqualTo(4);
  }
}
