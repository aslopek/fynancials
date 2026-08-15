package de.as.traquity.depot.position;

import de.as.traquity.common.config.MapStructConfig;
import de.as.traquity.depot.position.api.model.LotDto;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
interface LotMapper {

  LotDto toDto(Lot lot);
}
