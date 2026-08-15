package de.as.traquity.depot.performance.mapper;

import de.as.traquity.common.config.MapStructConfig;
import de.as.traquity.depot.performance.api.model.PerformanceDto;
import de.as.traquity.depot.performance.api.model.TransactionReferenceDto;
import de.as.traquity.depot.performance.model.Performance;
import de.as.traquity.depot.transaction.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface PerformanceMapper {


  PerformanceDto toPerformanceDto(Performance performance);

  @Mapping(target = "transactionId", source = "id")
  TransactionReferenceDto toTransactionReferenceDto(Transaction transaction);
}
