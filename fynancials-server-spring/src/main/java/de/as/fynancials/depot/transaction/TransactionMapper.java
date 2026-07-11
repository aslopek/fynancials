package de.as.fynancials.depot.transaction;

import de.as.fynancials.common.config.MapStructConfig;
import de.as.fynancials.common.pagination.PageContainer;
import de.as.fynancials.depot.transaction.api.model.PaginatedTransactionReadDto;
import de.as.fynancials.depot.transaction.api.model.TransactionCreateDto;
import de.as.fynancials.depot.transaction.api.model.TransactionReadDto;
import de.as.fynancials.depot.transaction.api.model.TransactionUpdateDto;
import java.math.MathContext;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(config = MapStructConfig.class)
abstract class TransactionMapper {

  @Autowired
  private MathContext mathContext;

  abstract Transaction fromEntity(TransactionEntity entity);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  abstract TransactionEntity toEntity(Transaction transaction);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "depotId", ignore = true)
  abstract Transaction fromCreateDto(TransactionCreateDto dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "depotId", ignore = true)
  abstract Transaction fromUpdateDto(TransactionUpdateDto dto);

  @Mapping(target = "netValue", source = "transaction", qualifiedByName = "getTransactionNetValue")
  abstract TransactionReadDto toDto(Transaction transaction);

  abstract PaginatedTransactionReadDto toPageDto(PageContainer<Transaction> pageContainer);

  @Named("getTransactionNetValue")
  protected double getTransactionNetValue(Transaction transaction) {
    return transaction.getNetValue(mathContext).doubleValue();
  }
}
