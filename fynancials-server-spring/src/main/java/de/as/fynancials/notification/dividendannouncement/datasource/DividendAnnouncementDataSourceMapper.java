package de.as.fynancials.notification.dividendannouncement.datasource;

import static de.as.fynancials.notification.dividendannouncement.api.model.DateFormatDto.CUSTOM_STRING;
import static de.as.fynancials.notification.dividendannouncement.api.model.DateFormatDto.TIMESTAMP_MILLISECONDS;
import static de.as.fynancials.notification.dividendannouncement.api.model.DateFormatDto.TIMESTAMP_SECONDS;

import de.as.fynancials.common.config.MapStructConfig;
import de.as.fynancials.common.error.BadRequestException;
import de.as.fynancials.notification.dividendannouncement.api.model.CurrencyMappingDto;
import de.as.fynancials.notification.dividendannouncement.api.model.DateConfigurationDto;
import de.as.fynancials.notification.dividendannouncement.api.model.DateFormatDto;
import de.as.fynancials.notification.dividendannouncement.api.model.DividendAnnouncementDataSourceCreateDto;
import de.as.fynancials.notification.dividendannouncement.api.model.DividendAnnouncementDataSourceReadDto;
import de.as.fynancials.notification.dividendannouncement.api.model.DividendAnnouncementDataSourceUpdateDto;
import de.as.fynancials.notification.dividendannouncement.api.model.RequestHeaderDto;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Slf4j
@Mapper(config = MapStructConfig.class)
abstract class DividendAnnouncementDataSourceMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  abstract DividendAnnouncementDataSource fromCreateDto(DividendAnnouncementDataSourceCreateDto dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  abstract DividendAnnouncementDataSource fromUpdateDto(DividendAnnouncementDataSourceUpdateDto dto);

  abstract DividendAnnouncementDataSourceReadDto toDto(DividendAnnouncementDataSource source);

  abstract DividendAnnouncementDataSource fromEntity(DividendAnnouncementDataSourceEntity dividendAnnouncementDataSourceEntity);

  abstract DividendAnnouncementDataSourceEntity toEntity(DividendAnnouncementDataSource dividendAnnouncementDataSource);

  protected abstract DividendAnnouncementCurrencyMapping fromCurrencyMappingEntity(CurrencyMappingEntity entity);

  protected abstract CurrencyMappingEntity toCurrencyMappingEntity(DividendAnnouncementCurrencyMapping currencyMapping);

  protected abstract DividendAnnouncementCurrencyMapping fromCurrencyMappingDto(CurrencyMappingDto currencyMappingDto);

  @Mapping(target = "currencyKey", ignore = true)
  protected abstract CurrencyMappingDto toCurrencyMappingDto(DividendAnnouncementCurrencyMapping currencyMapping);

  protected Map<String, DividendAnnouncementCurrencyMapping> fromCurrencyMappingDto(List<CurrencyMappingDto> list) {
    Map<String, DividendAnnouncementCurrencyMapping> result = new HashMap<>();
    if (list == null) {
      return result;
    }

    DividendAnnouncementCurrencyMapping currencyMapping;
    for (CurrencyMappingDto currencyMappingDto : list) {
      currencyMapping = fromCurrencyMappingDto(currencyMappingDto);
      result.put(currencyMappingDto.getCurrencyKey(), currencyMapping);
    }
    return result;
  }

  protected List<CurrencyMappingDto> toCurrencyMappingDto(Map<String, DividendAnnouncementCurrencyMapping> map) {
    List<CurrencyMappingDto> result = new LinkedList<>();
    if (map == null) {
      return result;
    }

    CurrencyMappingDto currencyMapping;
    for (Map.Entry<String, DividendAnnouncementCurrencyMapping> entry : map.entrySet()) {
      currencyMapping = toCurrencyMappingDto(entry.getValue());
      currencyMapping.setCurrencyKey(entry.getKey());
      result.add(currencyMapping);
    }
    return result;
  }

  protected String fromDateFormatDto(DateConfigurationDto dto) {
    if (dto == null) {
      return null;
    }
    DateFormatDto dateFormat = dto.getFormat();
    if (dateFormat == TIMESTAMP_SECONDS || dateFormat == TIMESTAMP_MILLISECONDS) {
      return dateFormat.getValue();
    } else if (dto.getCustomPattern() == null || dto.getCustomPattern().isEmpty()) {
      log.error("Illegal combination: dateFormat = {}, customPattern = {}", dateFormat, dto.getCustomPattern());
      throw new BadRequestException();
    }
    return dto.getCustomPattern();
  }

  protected DateConfigurationDto toDateFormatDto(String dateFormat) {
    if (dateFormat == null) {
      return null;
    }

    if (TIMESTAMP_SECONDS.getValue().equals(dateFormat)) {
      return new DateConfigurationDto(TIMESTAMP_SECONDS);
    } else if (TIMESTAMP_MILLISECONDS.getValue().equals(dateFormat)) {
      return new DateConfigurationDto(TIMESTAMP_MILLISECONDS);
    } else {
      DateConfigurationDto dateConfigurationDto = new DateConfigurationDto(CUSTOM_STRING);
      dateConfigurationDto.setCustomPattern(dateFormat);
      return dateConfigurationDto;
    }
  }

  protected Map<String, String> fromRequestHeaderDto(List<RequestHeaderDto> dtos) {
    Map<String, String> result = new HashMap<>();
    if (dtos == null) {
      return result;
    }

    for (RequestHeaderDto dto : dtos) {
      result.put(dto.getHeaderName(), dto.getHeaderValue());
    }
    return result;
  }

  protected List<RequestHeaderDto> toRequestHeaderDto(Map<String, String> map) {
    List<RequestHeaderDto> result = new LinkedList<>();
    if (map == null) {
      return result;
    }

    for (Map.Entry<String, String> entry : map.entrySet()) {
      result.add(new RequestHeaderDto(entry.getKey(), entry.getValue()));
    }
    return result;
  }
}
