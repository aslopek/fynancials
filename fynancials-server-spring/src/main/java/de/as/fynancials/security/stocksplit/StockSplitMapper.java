package de.as.fynancials.security.stocksplit;

import de.as.fynancials.common.config.MapStructConfig;
import de.as.fynancials.security.api.model.StockSplitDto;
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
