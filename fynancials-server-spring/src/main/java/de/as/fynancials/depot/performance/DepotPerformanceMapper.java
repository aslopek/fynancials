package de.as.fynancials.depot.performance;

import de.as.fynancials.common.config.MapStructConfig;
import de.as.fynancials.depot.performance.api.model.DepotPerformanceDto;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
interface DepotPerformanceMapper {

  DepotPerformanceDto toDepotPerformanceDto(DepotPerformance depotPerformance);
}
