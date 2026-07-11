package de.as.fynancials.security.stocksplit;

import de.as.fynancials.security.api.controller.StockSplitApiDelegate;
import de.as.fynancials.security.api.model.StockSplitDto;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
class StockSplitController implements StockSplitApiDelegate {

  private static final String URI_PATTERN = "/securities/%d/stock-splits";

  private final StockSplitService stockSplitService;
  private final StockSplitMapper stockSplitMapper;

  @Override
  public ResponseEntity<StockSplitDto> createStockSplit(Long id, Boolean updateTransactions,
                                                        Boolean updateHistoricalPrices, StockSplitDto stockSplitDto) {
    StockSplit stockSplit = stockSplitMapper.fromDto(stockSplitDto);
    stockSplit.setSecurityId(id);
    stockSplitService.createStockSplit(stockSplit, updateTransactions, updateHistoricalPrices);
    URI uri = URI.create(String.format(URI_PATTERN, id));
    return ResponseEntity.created(uri).body(stockSplitDto);
  }

  @Override
  public ResponseEntity<List<StockSplitDto>> getStockSplits(Long id) {
    List<StockSplit> splits = stockSplitService.getStockSplits(id);
    List<StockSplitDto> responseBody = splits.stream().map(stockSplitMapper::toDto).toList();
    return ResponseEntity.ok(responseBody);
  }
}
