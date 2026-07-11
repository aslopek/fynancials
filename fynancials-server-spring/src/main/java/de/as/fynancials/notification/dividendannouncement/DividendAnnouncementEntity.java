package de.as.fynancials.notification.dividendannouncement;

import de.as.fynancials.common.database.converter.BigDecimalConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

@Data
@Entity
@Table(name = "dividend_announcement")
class DividendAnnouncementEntity {

  @Id
  @Column
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @CreationTimestamp
  @Column(updatable = false)
  private OffsetDateTime createdAt;

  @Column(nullable = false)
  private Long dataSourceId;

  @Column(nullable = false)
  private Long securityId;

  @Column(nullable = false)
  private boolean isNew;

  @Column(nullable = false)
  private LocalDate payDate;

  @Column(nullable = false)
  @Convert(converter = BigDecimalConverter.class)
  private BigDecimal amountPerShare;

  @Column(nullable = false)
  private String currency;
}
