package de.as.fynancials.price.security.historical;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface HistoricalSecurityPriceRepository extends JpaRepository<HistoricalSecurityPriceEntity, Long> {

  Optional<HistoricalSecurityPriceEntity> findFirstBySecurityIdOrderByDateDesc(Long securityId);

  List<HistoricalSecurityPriceEntity> findAllBySecurityIdAndDateGreaterThanEqualOrderByDateAsc(Long securityId,
                                                                                               LocalDate date);

  List<HistoricalSecurityPriceEntity> findAllBySecurityIdAndDateLessThanOrderByDateDesc(Long security, LocalDate date);

  @Transactional
  void deleteAllBySecurityId(Long securityId);
}
