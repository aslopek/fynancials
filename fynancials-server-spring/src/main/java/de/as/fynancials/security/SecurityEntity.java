package de.as.fynancials.security;

import de.as.fynancials.security.api.model.SecurityTypeDto;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.util.Set;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Entity
@Table(name = "security")
class SecurityEntity {

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

  @Column
  @Version
  private Long version;

  @Column(nullable = false, unique = true)
  private String isin;

  @Column(name = "symbol")
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "security_symbol", joinColumns = {@JoinColumn(name = "security_id")})
  private Set<String> symbols;

  @Column(nullable = false)
  private String name;

  @Column(unique = true)
  private String wkn;

  @Column
  private String sector;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private SecurityTypeDto securityType;
}
