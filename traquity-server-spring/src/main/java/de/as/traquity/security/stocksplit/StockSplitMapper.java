package de.as.traquity.security.stocksplit;

import de.as.traquity.common.config.MapStructConfig;
import de.as.traquity.security.api.model.StockSplitDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
interface StockSplitMapper {

  StockSplit fromEntity(StockSplitEntity stockSplitEntity);

  @Mapping(target = "securityId", ignore = true)
  StockSplit fromDto(StockSplitDto stockSplitDto);

  StockSplitDto toDto(StockSplit stockSplit);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  StockSplitEntity toEntity(StockSplit stockSplit);
}
