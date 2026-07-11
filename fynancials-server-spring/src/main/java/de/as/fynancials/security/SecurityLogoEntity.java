package de.as.fynancials.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Entity
@Table(name = "security_logo")
class SecurityLogoEntity {

  @Id
  @Column
  private Long id;

  @CreationTimestamp
  @Column(updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  @Lob
  @Column(nullable = false)
  private byte[] logo;
}
