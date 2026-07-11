package de.as.fynancials.security.stocksplit;

import de.as.fynancials.common.error.BadRequestException;
import de.as.fynancials.common.error.ConflictException;
import de.as.fynancials.common.error.NoContentException;
import de.as.fynancials.common.error.NotFoundException;
import java.util.List;

public interface StockSplitService {

  List<StockSplit> getStockSplits(Long securityId) throws NoContentException, NotFoundException;

  void createStockSplit(StockSplit stockSplit, boolean updateTransactions, boolean updateHistoricalPrices)
      throws BadRequestException, ConflictException, NotFoundException;
}
