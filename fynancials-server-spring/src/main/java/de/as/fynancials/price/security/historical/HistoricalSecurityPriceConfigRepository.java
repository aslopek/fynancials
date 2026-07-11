package de.as.fynancials.price.security.historical;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface HistoricalSecurityPriceConfigRepository extends JpaRepository<HistoricalSecurityPriceConfigEntity, Long> {

  Optional<HistoricalSecurityPriceConfigEntity> findBySecurityId(Long securityId);
  List<HistoricalSecurityPriceConfigEntity> findAllByActiveIsTrue();
}
