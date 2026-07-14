package de.as.fynancials.depot.transaction;

import de.as.fynancials.common.database.converter.BigDecimalConverter;
import de.as.fynancials.depot.transaction.api.model.TransactionTypeDto;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "transaction")
class TransactionEntity {

  @Id
  @Column
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @CreationTimestamp
  @Column(updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  @Version
  @Column(nullable = false)
  private Long version;

  @Column(nullable = false)
  private LocalDate date;

  @Column
  private LocalTime time;

  @Column(nullable = false)
  private Long depotId;

  @Column(nullable = false)
  private Long securityId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private TransactionTypeDto transactionType;

  @Column(nullable = false)
  @Convert(converter = BigDecimalConverter.class)
  private BigDecimal securityCountOriginal;

  @Column
  @Convert(converter = BigDecimalConverter.class)
  private BigDecimal securityCountSplitAdjusted;

  @Column(nullable = false)
  @Convert(converter = BigDecimalConverter.class)
  private BigDecimal grossValue;

  @Column
  @Convert(converter = BigDecimalConverter.class)
  private BigDecimal tax;

  @Column
  @Convert(converter = BigDecimalConverter.class)
  private BigDecimal fee;
}
