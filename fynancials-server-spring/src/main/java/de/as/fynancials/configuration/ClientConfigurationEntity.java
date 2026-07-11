package de.as.fynancials.configuration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Entity
@Table(name = "client_configuration")
class ClientConfigurationEntity {

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

  @Column(nullable = false, unique = true)
  private String clientId;

  @Column(nullable = false)
  private String configKey;

  @Lob
  @Column(nullable = false)
  private String configValue;
}
