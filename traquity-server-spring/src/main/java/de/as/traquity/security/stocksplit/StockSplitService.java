package de.as.traquity.security.stocksplit;

import de.as.traquity.common.error.BadRequestException;
import de.as.traquity.common.error.ConflictException;
import de.as.traquity.common.error.NotFoundException;
import java.util.List;

public interface StockSplitService {

  List<StockSplit> getStockSplits(Long securityId) throws NotFoundException;

  void createStockSplit(StockSplit stockSplit, boolean updateTransactions, boolean updateHistoricalPrices)
      throws BadRequestException, ConflictException, NotFoundException;
}
