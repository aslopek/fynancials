package de.as.fynancials.depot.transaction;

import static de.as.fynancials.common.pagination.PaginationUtils.getPageNumber;
import static de.as.fynancials.common.pagination.PaginationUtils.getPageSize;

import de.as.fynancials.common.error.BadRequestException;
import de.as.fynancials.common.pagination.PageContainer;
import de.as.fynancials.depot.transaction.api.controller.TransactionApiDelegate;
import de.as.fynancials.depot.transaction.api.model.PaginatedTransactionReadDto;
import de.as.fynancials.depot.transaction.api.model.SortOrderDto;
import de.as.fynancials.depot.transaction.api.model.TransactionCreateDto;
import de.as.fynancials.depot.transaction.api.model.TransactionReadDto;
import de.as.fynancials.depot.transaction.api.model.TransactionTypeDto;
import de.as.fynancials.depot.transaction.api.model.TransactionUpdateDto;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
class TransactionController implements TransactionApiDelegate {

  private static final String TRANSACTION_URI_PATTERN = "/depots/%d/transactions/%d";

  private final TransactionMapper transactionMapper;
  private final TransactionService transactionService;

  @Override
  public ResponseEntity<TransactionReadDto> createTransaction(Long depotId, TransactionCreateDto transactionCreateDto) {
    Transaction transaction =
        transactionService.createTransaction(depotId, transactionMapper.fromCreateDto(transactionCreateDto));
    TransactionReadDto responseBody = transactionMapper.toDto(transaction);
    URI locationHeader =
        URI.create(String.format(TRANSACTION_URI_PATTERN, transaction.getDepotId(), transaction.getId()));
    return ResponseEntity.created(locationHeader).body(responseBody);
  }

  @Override
  public ResponseEntity<TransactionReadDto> getTransaction(Long depotId, Long transactionId) {
    Transaction transaction = transactionService.getTransactions(depotId, transactionId);
    TransactionReadDto responseBody = transactionMapper.toDto(transaction);
    return ResponseEntity.ok(responseBody);
  }

  @Override
  public ResponseEntity<PaginatedTransactionReadDto> getTransactions(Long depotId, Integer page, Integer pageSize,
                                                                     List<TransactionTypeDto> transactionTypes,
                                                                     SortOrderDto orderByTime, LocalDate minDate,
                                                                     LocalDate maxDate, List<Long> securityIds) {
    if (transactionTypes != null && transactionTypes.isEmpty()) {
      throw new BadRequestException();
    }
    TransactionPageRequest pageRequest = new TransactionPageRequest();
    pageRequest.setPage(getPageNumber(page));
    pageRequest.setPageSize(getPageSize(pageSize));
    pageRequest.setDepotIds(Set.of(depotId));
    pageRequest.setTransactionTypes(transactionTypes == null ? null : Set.copyOf(transactionTypes));
    pageRequest.setOrderByTime(orderByTime);
    pageRequest.setMinDate(minDate);
    pageRequest.setMaxDate(maxDate);
    pageRequest.setSecurityIds(securityIds == null ? null : Set.copyOf(securityIds));

    PageContainer<Transaction> transactions = transactionService.getTransactions(pageRequest);
    PaginatedTransactionReadDto responseBody = transactionMapper.toPageDto(transactions);
    return ResponseEntity.ok(responseBody);
  }

  @Override
  public ResponseEntity<TransactionReadDto> updateTransaction(Long depotId, Long transactionId,
                                                              TransactionUpdateDto transactionUpdateDto) {
    Transaction transaction = transactionMapper.fromUpdateDto(transactionUpdateDto);
    transaction.setDepotId(depotId);
    transaction.setId(transactionId);
    transaction = transactionService.updateTransaction(transaction);
    TransactionReadDto responseBody = transactionMapper.toDto(transaction);
    return ResponseEntity.ok(responseBody);
  }

  @Override
  public ResponseEntity<Void> deleteTransaction(Long depotId, Long transactionId) {
    transactionService.deleteTransaction(depotId, transactionId);
    return ResponseEntity.noContent().build();
  }
}
