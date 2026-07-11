package de.as.fynancials.exchangerates;

import de.as.fynancials.common.config.ArithmeticConfig;
import de.as.fynancials.common.error.NotFoundException;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
class ExchangeRateServiceImpl implements ExchangeRateService {

  /**
   * Maximum age of an exchange rate - if it is older, it is considered to be outdated.
   */
  private static final long MAX_AGE = 7L;
  private final Supplier<EcbExchangeRateFetcher> ecbExchangeRateFetcherSupplier;
  private final ExchangeRateRepository exchangeRateRepository;
  private final ArithmeticConfig arithmeticConfig;

  @Transactional
  public void updateExchangeRates() {
    ExchangeRateEntity latestExchangeRate;
    try (Stream<ExchangeRateEntity> eurUsdRates =
             exchangeRateRepository.findAllByBaseCurrencyAndTargetCurrencyOrderByDateDesc("EUR", "USD")) {
      latestExchangeRate = eurUsdRates.findFirst().orElse(null);
    }
    LocalDate minDate = latestExchangeRate == null ? LocalDate.EPOCH : latestExchangeRate.getDate().plusDays(1);
    EcbExchangeRateFetcher fetcher = ecbExchangeRateFetcherSupplier.get();
    List<ExchangeRateEntity> exchangeRateEntities = fetcher.fetchExchangeRates(minDate);

    if (!exchangeRateEntities.isEmpty()) {
      exchangeRateRepository.saveAll(exchangeRateEntities);
      log.info("Added {} new exchange rates between {} and {}", exchangeRateEntities.size(),
          exchangeRateEntities.get(0).getDate(), exchangeRateEntities.get(exchangeRateEntities.size() - 1).getDate());
    }
  }

  @Override
  @Transactional
  public BigDecimal convert(BigDecimal value, String baseCurrency, String targetCurrency, LocalDate date)
      throws NotFoundException, OutdatedExchangeRateException {
    if (baseCurrency.equals(targetCurrency)) {
      return value;
    }
    Optional<ExchangeRateEntity> exchangeRate;
    try (Stream<ExchangeRateEntity> exchangeRates =
        exchangeRateRepository.findAllByBaseCurrencyAndTargetCurrencyAndDateLessThanEqualOrderByDateDesc(baseCurrency,
            targetCurrency, date)) {
      exchangeRate = exchangeRates.findFirst();
    }
    BigDecimal multiplier;

    if (exchangeRate.isPresent()) {
      multiplier = exchangeRate.get().getExchangeRate();
    } else {
      try (Stream<ExchangeRateEntity> exchangeRates =
               exchangeRateRepository.findAllByBaseCurrencyAndTargetCurrencyAndDateLessThanEqualOrderByDateDesc(
                   targetCurrency, baseCurrency, date)) {
        exchangeRate = exchangeRates.findFirst();
      }

      if (exchangeRate.isPresent()) {
        multiplier = BigDecimal.ONE.divide(exchangeRate.get().getExchangeRate(), arithmeticConfig.mathContext());
      } else {
        log.error("No exchange rate found for {} to {} at {}", baseCurrency, targetCurrency, date);
        throw new NotFoundException();
      }
    }

    long exchangeRateAgeInDays = ChronoUnit.DAYS.between(date, exchangeRate.get().getDate());
    BigDecimal result = value.multiply(multiplier, arithmeticConfig.mathContext());
    if (Math.abs(exchangeRateAgeInDays) <= MAX_AGE) {
      return result;
    } else {
      ExchangeRateEntity entity = exchangeRate.get();
      throw OutdatedExchangeRateException.builder().date(entity.getDate()).baseCurrency(entity.getBaseCurrency())
          .targetCurrency(entity.getTargetCurrency()).exchangeRate(entity.getExchangeRate()).conversionResult(result)
          .build();
    }
  }
}
