package de.as.fynancials.depot;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

interface DepotLogoRepository extends JpaRepository<DepotLogoEntity, Long> {

  @Transactional
  void deleteById(Long id);
}
