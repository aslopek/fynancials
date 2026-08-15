package de.as.traquity.depot;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

interface DepotLogoRepository extends JpaRepository<DepotLogoEntity, Long> {

  @Transactional
  void deleteById(Long id);
}
