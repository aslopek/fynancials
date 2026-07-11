package de.as.fynancials.depot.dividend;

import de.as.fynancials.common.config.MapStructConfig;
import de.as.fynancials.depot.dividend.api.model.DividendDto;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
interface DividendMapper {

  DividendDto toDto(Dividend dividend);
}
