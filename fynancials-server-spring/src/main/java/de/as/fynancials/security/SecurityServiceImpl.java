package de.as.fynancials.security;

import static de.as.fynancials.common.arithmetic.MathFunctions.cagr;

import de.as.fynancials.common.error.BadRequestException;
import de.as.fynancials.common.error.ConflictException;
import de.as.fynancials.common.error.InternalServerErrorException;
import de.as.fynancials.common.error.NoContentException;
import de.as.fynancials.common.error.NotFoundException;
import de.as.fynancials.common.error.UnprocessableEntityException;
import de.as.fynancials.common.image.ImageService;
import de.as.fynancials.common.pagination.PageContainer;
import de.as.fynancials.price.security.historical.HistoricalSecurityPrice;
import de.as.fynancials.price.security.historical.HistoricalSecurityPriceService;
import de.as.fynancials.security.api.model.PriceMetaInfoDto;
import de.as.fynancials.security.api.model.SecurityOrderPropertyDto;
import de.as.fynancials.security.api.model.SortOrderDto;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
class SecurityServiceImpl implements SecurityService {

  private final Clock clock;
  private final SecurityMapper securityMapper;
  private final SecurityRepository securityRepository;
  private final SecurityLogoRepository securityLogoRepository;
  private final HistoricalSecurityPriceService historicalSecurityPriceService;
  private final ImageService imageService;
  private final MathContext mathContext;

  @Override
  public Security createSecurity(Security security)
      throws BadRequestException, ConflictException, InternalServerErrorException {
    if (security.getId() != null) {
      throw new BadRequestException();
    }

    processSecurity(security);
    checkDuplicatesBeforeCreate(security);
    SecurityEntity securityEntity = securityMapper.toEntity(security);
    securityEntity.setVersion(null);
    securityEntity = persist(securityEntity);
    return securityMapper.fromEntity(securityEntity);
  }

  @Override
  public void deleteSecurity(Long securityId) throws NotFoundException {
    if (!securityRepository.existsById(securityId)) {
      throw new NotFoundException();
    }
    securityRepository.deleteById(securityId);
  }

  @Override
  public Map<Long, String> getNamesById() {
    List<SecurityEntity> securities = securityRepository.findAll();
    Map<Long, String> namesById = new HashMap<>(securities.size());
    for (SecurityEntity security : securities) {
      namesById.put(security.getId(), security.getName());
    }
    return namesById;
  }

  @Override
  public Map<Long, String> getNamesById(Set<Long> securityIds) {
    List<SecurityEntity> securities = securityRepository.findAllById(securityIds);
    Map<Long, String> namesById = new HashMap<>(securities.size());
    for (SecurityEntity security : securities) {
      namesById.put(security.getId(), security.getName());
    }
    return namesById;
  }

  @Override
  public PageContainer<Security> getSecurities(int page, int pageSize, String search, SecurityOrderPropertyDto orderBy,
                                               SortOrderDto order) throws NoContentException {
    final PageRequest pageRequest;
    if (orderBy != null) {
      Sort.Direction direction = SortOrderDto.DESC.equals(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
      String propertyName = SecurityOrderPropertyDto.NAME.equals(orderBy) ? "name" : "sector";
      pageRequest = PageRequest.of(page, pageSize, direction, propertyName);
    } else {
      pageRequest = PageRequest.of(page, pageSize);
    }

    final Page<SecurityEntity> securities;
    if (search != null && !search.isBlank()) {
      securities =
          securityRepository.findAllByNameContainingIgnoreCaseOrSectorContainingIgnoreCaseOrSymbolsContainingIgnoreCase(
              search, search, search, pageRequest);
    } else {
      securities = securityRepository.findAll(pageRequest);
    }
    if (securities.isEmpty()) {
      throw new NoContentException();
    }
    PageContainer<Security> result = new PageContainer<>();
    result.setTotal(securities.getTotalElements());
    result.setCurrentPage(page);
    result.setLastPage(securities.getTotalPages() - 1);
    result.setPageSize(pageSize);
    result.setItems(securities.stream().map(securityMapper::fromEntity).toList());
    result.getItems().forEach(security -> security.setPriceMetaInfo(getPriceMetaInfo(security.getId())));
    return result;
  }

  @Override
  public boolean securityExists(long securityId) {
    return securityRepository.existsById(securityId);
  }

  @Override
  public Security getSecurity(Long securityId) throws NotFoundException {
    SecurityEntity fromDatabase = securityRepository.findById(securityId).orElseThrow(NotFoundException::new);
    Security security = securityMapper.fromEntity(fromDatabase);
    security.setPriceMetaInfo(getPriceMetaInfo(securityId));
    return security;
  }

  @Override
  public Security getSecurityByIsin(String isin) throws NotFoundException {
    SecurityEntity fromDatabase = securityRepository.findByIsin(isin).orElseThrow(NotFoundException::new);
    Security security = securityMapper.fromEntity(fromDatabase);
    security.setPriceMetaInfo(getPriceMetaInfo(security.getId()));
    return security;
  }

  @Override
  public Security updateSecurity(Security security)
      throws BadRequestException, ConflictException, NotFoundException, InternalServerErrorException {
    if (!securityRepository.existsById(security.getId())) {
      throw new NotFoundException();
    }
    processSecurity(security);
    SecurityEntity updatedValues = securityMapper.toEntity(security);
    updatedValues.setId(security.getId());
    checkDuplicatesBeforeUpdate(updatedValues);
    updatedValues = persist(updatedValues);
    return securityMapper.fromEntity(updatedValues);
  }

  @Override
  public boolean hasLogo(Long securityId) {
    return securityLogoRepository.existsById(securityId);
  }

  @Override
  public Resource getLogo(Long securityId) throws NotFoundException {
    Optional<SecurityLogoEntity> optionalLogo = securityLogoRepository.findById(securityId);
    if (optionalLogo.isEmpty()) {
      throw new NotFoundException();
    }
    return new ByteArrayResource(optionalLogo.get().getLogo());
  }

  @Override
  public void setLogo(Long securityId, Resource logo) throws BadRequestException {
    if (!securityRepository.existsById(securityId)) {
      throw new BadRequestException();
    }

    byte[] pngBytes;

    try {
      pngBytes = logo.getInputStream().readAllBytes();
    } catch (IOException e) {
      throw new BadRequestException();
    }

    if (!imageService.isPng(pngBytes)) {
      throw new BadRequestException();
    }

    pngBytes = imageService.scaleImage(pngBytes);
    SecurityLogoEntity logoEntity = securityLogoRepository.findById(securityId).orElseGet(SecurityLogoEntity::new);
    logoEntity.setId(securityId);
    logoEntity.setLogo(pngBytes);
    securityLogoRepository.saveAndFlush(logoEntity);
  }

  @Override
  public void deleteLogo(Long securityId) {
    try {
      securityLogoRepository.deleteById(securityId);
    } catch (EmptyResultDataAccessException e) {
    }
  }

  @Override
  public BigDecimal getCagr(long securityId, LocalDate startDate, LocalDate endDate) throws BadRequestException, NotFoundException,
      UnprocessableEntityException {
    if (startDate == null) {
      throw new BadRequestException();
    } else if (endDate != null && (endDate.isBefore(startDate) || endDate.isEqual(startDate))) {
      throw new BadRequestException();
    } else if (!securityRepository.existsById(securityId)) {
      throw new NotFoundException();
    }

    final long toleranceInDays = 7;
    LocalDate firstDay = startDate.minusDays(toleranceInDays); // tolerance: accept prices a little older than the requested minDate
    LocalDate lastDay = endDate == null ? LocalDate.now(clock) : endDate;
    List<HistoricalSecurityPrice> prices = historicalSecurityPriceService.getPrices(securityId, firstDay);

    HistoricalSecurityPrice priceAtStart = findPriceClosestTo(prices, startDate);
    HistoricalSecurityPrice priceAtEnd = findPriceClosestTo(prices, lastDay);

    if (priceAtStart == null || priceAtEnd == null) {
      throw new UnprocessableEntityException();
    }

    long priceAtStartDateDifference = Math.abs(ChronoUnit.DAYS.between(priceAtStart.getDate(), startDate));
    long priceAtEndDateDifference = Math.abs(ChronoUnit.DAYS.between(priceAtEnd.getDate(), lastDay));
    if (priceAtStartDateDifference > toleranceInDays
        || priceAtEndDateDifference > toleranceInDays
        || priceAtStart.getDate().isEqual(priceAtEnd.getDate())) {
      throw new UnprocessableEntityException();
    }

    return cagr(priceAtStart.getPrice(), priceAtStart.getDate(), priceAtEnd.getPrice(), priceAtEnd.getDate(), mathContext);
  }

  private SecurityEntity persist(SecurityEntity security) throws ConflictException, InternalServerErrorException {
    SecurityEntity saved;
    try {
      saved = securityRepository.saveAndFlush(security);
    } catch (ObjectOptimisticLockingFailureException e) {
      throw new ConflictException();
    } catch (Exception e) {
      log.error(e.getClass().getName() + ": " + e.getMessage());
      throw new InternalServerErrorException();
    }
    return saved;
  }

  private void processSecurity(Security security) throws BadRequestException {
    if (security.getName() == null || security.getName().isBlank()) {
      throw new BadRequestException();
    }

    security.setName(security.getName().trim());
    security.setSymbols(security.getSymbols().stream().map(String::trim).toList());
    if (security.getSector() != null) {
      if (security.getSector().isBlank()) {
        throw new BadRequestException();
      }
      security.setSector(security.getSector().trim());
    }
  }

  private void checkDuplicatesBeforeCreate(Security security) throws ConflictException {
    if (securityRepository.existsByIsin(security.getIsin())) {
      throw new ConflictException();
    }

    if (!securityRepository.findSecurityIdsBySymbols(security.getSymbols()).isEmpty()) {
      throw new ConflictException();
    }
  }

  private void checkDuplicatesBeforeUpdate(SecurityEntity securityEntity) throws ConflictException {
    Optional<SecurityEntity> other = securityRepository.findByIsin(securityEntity.getIsin());
    if (other.isPresent() && !other.get().getId().equals(securityEntity.getId())) {
      throw new ConflictException();
    }

    if (securityEntity.getWkn() != null) {
      other = securityRepository.findByWkn(securityEntity.getWkn());
      if (other.isPresent() && !other.get().getId().equals(securityEntity.getId())) {
        throw new ConflictException();
      }
    }

    for (Long securityId : securityRepository.findSecurityIdsBySymbols(securityEntity.getSymbols())) {
      if (!securityId.equals(securityEntity.getId())) {
        throw new ConflictException();
      }
    }
  }

  private PriceMetaInfoDto getPriceMetaInfo(Long securityId) {
    LocalDate oneYearAgo = LocalDate.now(clock).minusYears(1);
    List<HistoricalSecurityPrice> prices;
    try {
      prices = historicalSecurityPriceService.getPrices(securityId, oneYearAgo);
    } catch (RuntimeException e) {
      return null;
    }
    if (prices.isEmpty()) {
      return null;
    }

    PriceMetaInfoDto priceMetaInfo = new PriceMetaInfoDto();
    HistoricalSecurityPrice latestPrice = prices.get(prices.size() - 1);
    priceMetaInfo.setCurrency(latestPrice.getCurrency());
    priceMetaInfo.setLatestPrice(latestPrice.getPrice().doubleValue());
    priceMetaInfo.setLatestPriceDate(latestPrice.getDate());

    List<Double> priceValues = new ArrayList<>(prices.stream().map(price -> price.getPrice().doubleValue()).toList());
    priceValues.sort(Double::compareTo);
    priceMetaInfo.setLowTrailingTwelveMonths(priceValues.get(0));
    priceMetaInfo.setHighTrailingTwelveMonths(priceValues.get(priceValues.size() - 1));
    return priceMetaInfo;
  }

  private HistoricalSecurityPrice findPriceClosestTo(List<HistoricalSecurityPrice> prices, LocalDate date) {
    if (prices.isEmpty()) {
      return null;
    }

    long currentDelta;
    long previousDelta = Long.MAX_VALUE;
    HistoricalSecurityPrice closestPrice = prices.getFirst();

    for (HistoricalSecurityPrice price : prices) {
      currentDelta = Math.abs(ChronoUnit.DAYS.between(date, price.getDate()));

      if (currentDelta < previousDelta) {
        closestPrice = price;
        previousDelta = currentDelta;
      } else if (currentDelta > previousDelta) {
        break;
      }
    }

    return closestPrice;
  }
}
