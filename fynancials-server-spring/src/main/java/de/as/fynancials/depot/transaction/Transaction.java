package de.as.fynancials.depot.transaction;

import static de.as.fynancials.depot.transaction.api.model.TransactionTypeDto.DIVIDEND;
import static de.as.fynancials.depot.transaction.api.model.TransactionTypeDto.SELL;
import static de.as.fynancials.depot.transaction.api.model.TransactionTypeDto.SPECIAL_DIVIDEND;

import de.as.fynancials.depot.transaction.api.model.TransactionTypeDto;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import lombok.Data;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
@Data
public class Transaction implements Comparable<Transaction> {

  private Long id;
  private Long version;
  private LocalDate date;
  private LocalTime time;
  private Long depotId;
  private Long securityId;
  private TransactionTypeDto transactionType;
  private BigDecimal securityCountOriginal;
  private BigDecimal securityCountSplitAdjusted;
  private BigDecimal grossValue;
  private BigDecimal tax;
  private BigDecimal fee;

  public BigDecimal getCountForArithmeticOperations() {
    return securityCountSplitAdjusted != null ? securityCountSplitAdjusted : securityCountOriginal;
  }

  public BigDecimal getNetValue(MathContext mathContext) {
    BigDecimal tax = this.tax == null ? BigDecimal.ZERO : this.tax;
    BigDecimal fee = this.fee == null ? BigDecimal.ZERO : this.fee;

    if (Set.of(SELL, DIVIDEND, SPECIAL_DIVIDEND).contains(transactionType)) {
      return grossValue.subtract(tax, mathContext).subtract(fee, mathContext);
    } else {
      return grossValue.add(tax, mathContext).add(fee, mathContext);
    }
  }

  public int getYear() {
    return date.getYear();
  }

  public int getMonth() {
    return date.getMonthValue();
  }

  public int getQuarter() {
    return ((date.getMonthValue() - 1) / 3) + 1;
  }

  @Override
  public int compareTo(Transaction other) {
    if (other == null) {
      throw new NullPointerException();
    }

    int compareResult = date.compareTo(other.date);
    if (compareResult == 0 && time != null && other.time != null) {
      compareResult = time.compareTo(other.time);
    }
    if (compareResult == 0) {
      compareResult = id.compareTo(other.id);
    }
    return compareResult;
  }
}
