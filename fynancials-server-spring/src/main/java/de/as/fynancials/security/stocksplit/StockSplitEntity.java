package de.as.fynancials.security.stocksplit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Entity
@Table(name = "stock_split")
class StockSplitEntity {

  @Id
  @Column
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @CreationTimestamp
  @Column(updatable = false)
  private OffsetDateTime createdAt;

  @Column
  @UpdateTimestamp
  private OffsetDateTime updatedAt;

  @Column(nullable = false)
  private Long securityId;

  @Column(nullable = false)
  private LocalDate exDate;

  @Column(nullable = false)
  private Long quantityOld;

  @Column(nullable = false)
  private Long quantityNew;
}
