package de.as.fynancials.notification.dividendannouncement.config;

import de.as.fynancials.common.config.MapStructConfig;
import de.as.fynancials.notification.dividendannouncement.api.model.DividendAnnouncementConfigCreateDto;
import de.as.fynancials.notification.dividendannouncement.api.model.DividendAnnouncementConfigReadDto;
import de.as.fynancials.notification.dividendannouncement.api.model.DividendAnnouncementConfigUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
interface DividendAnnouncementConfigMapper {

  DividendAnnouncementConfig fromEntity(DividendAnnouncementConfigEntity entity);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  DividendAnnouncementConfigEntity toEntity(DividendAnnouncementConfig config);

  @Mapping(target = "version", ignore = true)
  @Mapping(target = "securityId", ignore = true)
  @Mapping(target = "active", source = "isActive")
  DividendAnnouncementConfig fromCreateDto(DividendAnnouncementConfigCreateDto dto);

  @Mapping(target = "securityId", ignore = true)
  @Mapping(target = "active", source = "isActive")
  DividendAnnouncementConfig fromUpdateDto(DividendAnnouncementConfigUpdateDto dto);

  @Mapping(target = "isActive", source = "active")
  DividendAnnouncementConfigReadDto toDto(DividendAnnouncementConfig config);
}
