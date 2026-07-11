package de.as.fynancials.depot.performance.mapper;

import de.as.fynancials.common.config.MapStructConfig;
import de.as.fynancials.depot.performance.api.model.PerformanceDto;
import de.as.fynancials.depot.performance.api.model.TransactionReferenceDto;
import de.as.fynancials.depot.performance.model.Performance;
import de.as.fynancials.depot.transaction.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface PerformanceMapper {


  PerformanceDto toPerformanceDto(Performance performance);

  @Mapping(target = "transactionId", source = "id")
  TransactionReferenceDto toTransactionReferenceDto(Transaction transaction);
}
