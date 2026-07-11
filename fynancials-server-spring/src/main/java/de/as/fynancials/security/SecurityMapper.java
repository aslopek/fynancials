package de.as.fynancials.security;

import de.as.fynancials.common.config.MapStructConfig;
import de.as.fynancials.common.pagination.PageContainer;
import de.as.fynancials.security.api.model.PaginatedSecurityReadDto;
import de.as.fynancials.security.api.model.SecurityCreateDto;
import de.as.fynancials.security.api.model.SecurityReadDto;
import de.as.fynancials.security.api.model.SecurityUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
interface SecurityMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "priceMetaInfo", ignore = true)
  Security fromCreateDto(SecurityCreateDto securityWriteDto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "priceMetaInfo", ignore = true)
  Security fromUpdateDto(SecurityUpdateDto securityUpdateDto);

  @Mapping(target = "priceMetaInfo", ignore = true)
  Security fromEntity(SecurityEntity securityEntity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  SecurityEntity toEntity(Security security);

  @Mapping(target = "links", expression = "java(new de.as.fynancials.security.api.model.SecurityLinksDto())")
  SecurityReadDto toDto(Security security);

  PaginatedSecurityReadDto toPageDto(PageContainer<Security> pageContainer);
}
