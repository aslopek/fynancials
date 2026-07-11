package de.as.fynancials.security;

import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
interface SecurityRepository extends JpaRepository<SecurityEntity, Long> {

  @Transactional
  void deleteById(Long id);

  boolean existsByIsin(String isin);

  Page<SecurityEntity> findAllByNameContainingIgnoreCaseOrSectorContainingIgnoreCaseOrSymbolsContainingIgnoreCase(
      String name, String sector, String symbols, PageRequest pageRequest);

  Optional<SecurityEntity> findByIsin(String isin);

  Optional<SecurityEntity> findByWkn(String wkn);

  @Query("""
      SELECT DISTINCT s.id
      FROM SecurityEntity s
      JOIN s.symbols symbol
      WHERE symbol IN :symbols
      """)
  Set<Long> findSecurityIdsBySymbols(Collection<String> symbols);
}
