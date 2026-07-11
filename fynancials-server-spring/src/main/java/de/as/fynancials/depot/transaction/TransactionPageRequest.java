package de.as.fynancials.depot.transaction;

import de.as.fynancials.common.error.BadRequestException;
import de.as.fynancials.depot.transaction.api.model.SortOrderDto;
import de.as.fynancials.depot.transaction.api.model.TransactionTypeDto;
import java.time.LocalDate;
import java.util.Set;
import lombok.Data;

@Data
public class TransactionPageRequest {

  private Integer page;
  private Integer pageSize;
  private Set<Long> depotIds;
  private Set<TransactionTypeDto> transactionTypes;
  private SortOrderDto orderByTime;
  private LocalDate minDate;
  private LocalDate maxDate;
  private Set<Long> securityIds;

  void validate() throws BadRequestException {
    if (page == null || page < 0) {
      throw new BadRequestException();
    }
    if (pageSize == null || pageSize < 1) {
      throw new BadRequestException();
    }
    if (transactionTypes != null && transactionTypes.isEmpty()) {
      throw new BadRequestException();
    }
    if (securityIds != null && securityIds.isEmpty()) {
      throw new BadRequestException();
    }
    if (minDate != null && maxDate != null && minDate.isAfter(maxDate)) {
      throw new BadRequestException();
    }
  }
}
