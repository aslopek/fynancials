package de.as.fynancials.depot.position;

import de.as.fynancials.common.config.MapStructConfig;
import de.as.fynancials.depot.position.api.model.DepotCompositionDto;
import de.as.fynancials.depot.position.api.model.DepotPositionDto;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
interface DepotPositionMapper {

  DepotCompositionDto toDepotCompositionDto(DepotComposition depotComposition);

  DepotPositionDto toDepotPositionDto(DepotPosition depotPosition);
}
