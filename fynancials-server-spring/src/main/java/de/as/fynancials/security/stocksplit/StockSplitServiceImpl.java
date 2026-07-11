package de.as.fynancials.security.stocksplit;

import de.as.fynancials.common.error.BadRequestException;
import de.as.fynancials.common.error.ConflictException;
import de.as.fynancials.common.error.NoContentException;
import de.as.fynancials.common.error.NotFoundException;
import de.as.fynancials.depot.transaction.TransactionService;
import de.as.fynancials.price.security.historical.HistoricalSecurityPriceService;
import de.as.fynancials.security.SecurityService;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class StockSplitServiceImpl implements StockSplitService {

  private final StockSplitRepository stockSplitRepository;
  private final StockSplitMapper stockSplitMapper;
  private final SecurityService securityService;
  private final TransactionService transactionService;
  private final HistoricalSecurityPriceService historicalSecurityPriceService;
  private final Clock clock;
  private final MathContext mathContext;

  @Override
  public List<StockSplit> getStockSplits(Long securityId) throws NoContentException, NotFoundException {
    securityService.getSecurity(securityId);
    List<StockSplitEntity> splits = stockSplitRepository.findAllBySecurityIdOrderByExDateAsc(securityId);
    if (splits.isEmpty()) {
      throw new NoContentException();
    }
    return splits.stream().map(stockSplitMapper::fromEntity).toList();
  }

  @Override
  public void createStockSplit(StockSplit newStockSplit, boolean updateTransactions, boolean updateHistoricalPrices)
      throws BadRequestException, ConflictException, NotFoundException {
    boolean quantityNewOk =
        newStockSplit != null && newStockSplit.getQuantityNew() != null && newStockSplit.getQuantityNew() != 0.0;
    boolean quantityOldOk =
        newStockSplit != null && newStockSplit.getQuantityOld() != null && newStockSplit.getQuantityOld() != 0.0;
    boolean dateIsOk = newStockSplit != null && newStockSplit.getExDate() != null;
    if (!quantityNewOk || !quantityOldOk || !dateIsOk) {
      throw new BadRequestException();
    }

    long securityId = newStockSplit.getSecurityId();
    securityService.getSecurity(securityId);
    List<StockSplitEntity> splits = stockSplitRepository.findAllBySecurityIdOrderByExDateAsc(securityId);

    LocalDate exDate = newStockSplit.getExDate();
    if (!exDate.isBefore(LocalDate.now(clock))) {
      throw new BadRequestException();
    }

    for (StockSplitEntity split : splits) {
      if (exDate.isBefore(split.getExDate())) {
        throw new BadRequestException();
      } else if (exDate.isEqual(split.getExDate())) {
        throw new ConflictException();
      }
    }

    stockSplitRepository.save(stockSplitMapper.toEntity(newStockSplit));
    BigDecimal quantityNew = BigDecimal.valueOf(newStockSplit.getQuantityNew());
    BigDecimal quantityOld = BigDecimal.valueOf(newStockSplit.getQuantityOld());
    BigDecimal multiplier = quantityNew.divide(quantityOld, mathContext);

    if (updateTransactions) {
      transactionService.splitAdjustment(securityId, multiplier, exDate);
    }
    if (updateHistoricalPrices) {
      historicalSecurityPriceService.splitAdjustment(securityId, multiplier, exDate);
    }
  }
}
