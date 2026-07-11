package de.as.fynancials.configuration.securitygroup;

import de.as.fynancials.common.config.MapStructConfig;
import de.as.fynancials.config.securitygroup.api.model.SecurityGroupCreateDto;
import de.as.fynancials.config.securitygroup.api.model.SecurityGroupReadDto;
import de.as.fynancials.config.securitygroup.api.model.SecurityGroupUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
interface SecurityGroupMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  SecurityGroup fromCreateDto(SecurityGroupCreateDto securityGroupCreateDto);

  @Mapping(target = "id", ignore = true)
  SecurityGroup fromUpdateDto(SecurityGroupUpdateDto securityGroupUpdateDto);

  SecurityGroup fromEntity(SecurityGroupEntity securityGroupEntity);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  SecurityGroupEntity toEntity(SecurityGroup securityGroup);

  SecurityGroupReadDto toDto(SecurityGroup securityGroup);
}
