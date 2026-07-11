package de.as.fynancials.security.stocksplit;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface StockSplitRepository extends JpaRepository<StockSplitEntity, Long> {

  boolean existsByExDateAndSecurityId(LocalDate exDate, Long securityId);

  List<StockSplitEntity> findAllBySecurityIdOrderByExDateAsc(Long securityId);
}
