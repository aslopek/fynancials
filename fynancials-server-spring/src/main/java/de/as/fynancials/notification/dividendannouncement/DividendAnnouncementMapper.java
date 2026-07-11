package de.as.fynancials.notification.dividendannouncement;

import de.as.fynancials.common.config.MapStructConfig;
import de.as.fynancials.notification.dividendannouncement.api.model.DividendAnnouncementReadDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
interface DividendAnnouncementMapper {

  @Mapping(target = "dataSource", ignore = true)
  DividendAnnouncement fromEntity(DividendAnnouncementEntity entity);

  @Mapping(target = "dataSourceId", source = "dataSource.id")
  @Mapping(target = "isNew", source = "new")
  DividendAnnouncementReadDto toDto(DividendAnnouncement dividendAnnouncement);
}
