package de.as.fynancials.security;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface SecurityRepository extends JpaRepository<SecurityEntity, Long> {

  Page<SecurityEntity> findAllByNameContainingIgnoreCaseOrSectorContainingIgnoreCaseOrSymbolsContainingIgnoreCase(
      String name, String sector, String symbols, PageRequest pageRequest);

  Optional<SecurityEntity> findByIsin(String isin);
}
