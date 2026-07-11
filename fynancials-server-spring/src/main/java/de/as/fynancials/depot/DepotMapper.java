package de.as.fynancials.depot;

import de.as.fynancials.common.config.MapStructConfig;
import de.as.fynancials.depot.api.model.DepotCreateDto;
import de.as.fynancials.depot.api.model.DepotReadDto;
import de.as.fynancials.depot.api.model.DepotUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
interface DepotMapper {

  Depot fromEntity(DepotEntity depotEntity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  Depot fromCreateDto(DepotCreateDto depotCreateDto);

  @Mapping(target = "id", ignore = true)
  Depot fromUpdateDto(DepotUpdateDto depotUpdateDto);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  DepotEntity toEntity(Depot depot);

  @Mapping(target = "links", expression = "java(new de.as.fynancials.depot.api.model.DepotLinksDto())")
  DepotReadDto toDto(Depot depot);
}
