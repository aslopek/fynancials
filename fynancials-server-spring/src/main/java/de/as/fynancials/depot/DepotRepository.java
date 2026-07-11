package de.as.fynancials.depot;

import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface DepotRepository extends JpaRepository<DepotEntity, Long> {

  @Transactional
  void deleteById(Long id);

  boolean existsByName(String name);

  Optional<DepotEntity> findByName(String name);
}
