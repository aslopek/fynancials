package de.as.fynancials.depot.position;

import de.as.fynancials.common.config.MapStructConfig;
import de.as.fynancials.depot.position.api.model.LotDto;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
interface LotMapper {

  LotDto toDto(Lot lot);
}
