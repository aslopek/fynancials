package de.as.fynancials.price.security.historical;

import de.as.fynancials.common.config.MapStructConfig;
import de.as.fynancials.price.security.historical.api.model.HistoricalSecurityPriceConfigDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
interface HistoricalSecurityPriceConfigMapper {

  HistoricalSecurityPriceConfig fromEntity(HistoricalSecurityPriceConfigEntity entity);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  HistoricalSecurityPriceConfigEntity toEntity(HistoricalSecurityPriceConfig config);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "securityId", ignore = true)
  @Mapping(target = "active", source = "isActive")
  HistoricalSecurityPriceConfig fromDto(HistoricalSecurityPriceConfigDto dto);

  @Mapping(target = "isActive", source = "active")
  HistoricalSecurityPriceConfigDto toDto(HistoricalSecurityPriceConfig config);
}
