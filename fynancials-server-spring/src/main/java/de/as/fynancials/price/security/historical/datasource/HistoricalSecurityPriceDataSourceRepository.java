package de.as.fynancials.price.security.historical.datasource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface HistoricalSecurityPriceDataSourceRepository extends JpaRepository<HistoricalSecurityPriceDataSourceEntity, Long> {
}
