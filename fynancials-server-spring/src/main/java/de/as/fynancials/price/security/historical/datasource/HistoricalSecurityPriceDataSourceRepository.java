package de.as.fynancials.price.security.historical.datasource;

import org.springframework.data.jpa.repository.JpaRepository;

interface HistoricalSecurityPriceDataSourceRepository extends JpaRepository<HistoricalSecurityPriceDataSourceEntity, Long> {

  boolean existsByName(String name);

  boolean existsByNameAndIdNot(String name, Long id);
}
