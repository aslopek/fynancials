package de.as.traquity.depot.position;

import de.as.traquity.common.config.MapStructConfig;
import de.as.traquity.depot.position.api.model.DepotCompositionDto;
import de.as.traquity.depot.position.api.model.DepotPositionDto;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
interface DepotPositionMapper {

  DepotCompositionDto toDepotCompositionDto(DepotComposition depotComposition);

  DepotPositionDto toDepotPositionDto(DepotPosition depotPosition);
}
