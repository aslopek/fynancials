package de.as.traquity.depot.dividend;

import de.as.traquity.common.config.MapStructConfig;
import de.as.traquity.depot.dividend.api.model.DividendDto;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
interface DividendMapper {

  DividendDto toDto(Dividend dividend);
}
