package de.as.traquity.price.security.historical;

import de.as.traquity.common.config.MapStructConfig;
import de.as.traquity.price.security.historical.api.model.HistoricalSecurityPriceDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
abstract class HistoricalSecurityPriceMapper {

  abstract HistoricalSecurityPrice fromEntity(HistoricalSecurityPriceEntity entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  abstract HistoricalSecurityPriceEntity toEntity(HistoricalSecurityPrice entity);

  abstract HistoricalSecurityPriceDto toDto(HistoricalSecurityPrice price);
}
