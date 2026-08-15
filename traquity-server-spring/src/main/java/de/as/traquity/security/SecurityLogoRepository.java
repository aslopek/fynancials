package de.as.traquity.security;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

interface SecurityLogoRepository extends JpaRepository<SecurityLogoEntity, Long> {

  @Transactional
  void deleteById(Long id);
}
