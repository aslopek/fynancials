package de.as.fynancials.exchangerates;

import java.time.LocalDate;
import java.util.stream.Stream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ExchangeRateRepository extends JpaRepository<ExchangeRateEntity, Long> {

  Stream<ExchangeRateEntity> findAllByBaseCurrencyAndTargetCurrencyOrderByDateDesc(String baseCurrency,
                                                                                   String targetCurrency);

  Stream<ExchangeRateEntity> findAllByBaseCurrencyAndTargetCurrencyAndDateLessThanEqualOrderByDateDesc(
      String baseCurrency, String targetCurrency, LocalDate date);
}
