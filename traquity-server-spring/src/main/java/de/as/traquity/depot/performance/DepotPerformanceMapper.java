package de.as.traquity.depot.performance;

import de.as.traquity.common.config.MapStructConfig;
import de.as.traquity.depot.performance.api.model.DepotPerformanceDto;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
interface DepotPerformanceMapper {

  DepotPerformanceDto toDepotPerformanceDto(DepotPerformance depotPerformance);
}
