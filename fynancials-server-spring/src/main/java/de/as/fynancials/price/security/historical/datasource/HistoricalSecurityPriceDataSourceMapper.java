package de.as.fynancials.price.security.historical.datasource;

import static de.as.fynancials.price.security.historical.api.model.DateFormatDto.CUSTOM_STRING;
import static de.as.fynancials.price.security.historical.api.model.DateFormatDto.TIMESTAMP_MILLISECONDS;
import static de.as.fynancials.price.security.historical.api.model.DateFormatDto.TIMESTAMP_SECONDS;

import de.as.fynancials.common.config.MapStructConfig;
import de.as.fynancials.common.error.BadRequestException;
import de.as.fynancials.price.security.historical.api.model.CurrencyMappingDto;
import de.as.fynancials.price.security.historical.api.model.DateConfigurationDto;
import de.as.fynancials.price.security.historical.api.model.DateFormatDto;
import de.as.fynancials.price.security.historical.api.model.HistoricalSecurityPriceDataSourceCreateDto;
import de.as.fynancials.price.security.historical.api.model.HistoricalSecurityPriceDataSourceReadDto;
import de.as.fynancials.price.security.historical.api.model.HistoricalSecurityPriceDataSourceUpdateDto;
import de.as.fynancials.price.security.historical.api.model.HistoricalSecurityPriceUrlPatternsDto;
import de.as.fynancials.price.security.historical.api.model.RequestHeaderDto;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Slf4j
@Mapper(config = MapStructConfig.class)
abstract class HistoricalSecurityPriceDataSourceMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "urlPatterns", qualifiedByName = "fromHistoricalSecurityPriceUrlPatternDto")
  abstract HistoricalSecurityPriceDataSource fromCreateDto(HistoricalSecurityPriceDataSourceCreateDto dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "urlPatterns", qualifiedByName = "fromHistoricalSecurityPriceUrlPatternDto")
  abstract HistoricalSecurityPriceDataSource fromUpdateDto(HistoricalSecurityPriceDataSourceUpdateDto dto);

  @Mapping(target = "urlPatterns", qualifiedByName = "toHistoricalSecurityPriceUrlPatternsDto")
  abstract HistoricalSecurityPriceDataSourceReadDto toDto(HistoricalSecurityPriceDataSource source);

  abstract HistoricalSecurityPriceDataSource fromEntity(HistoricalSecurityPriceDataSourceEntity historicalSecurityPriceDataSourceEntity);

  abstract HistoricalSecurityPriceDataSourceEntity toEntity(HistoricalSecurityPriceDataSource historicalSecurityPriceDataSource);

  protected abstract HistoricalSecurityPriceCurrencyMapping fromCurrencyMappingEntity(CurrencyMappingEntity entity);

  protected abstract CurrencyMappingEntity toCurrencyMappingEntity(HistoricalSecurityPriceCurrencyMapping currencyMapping);

  protected abstract HistoricalSecurityPriceCurrencyMapping fromCurrencyMappingDto(CurrencyMappingDto currencyMappingDto);

  @Mapping(target = "currencyKey", ignore = true)
  protected abstract CurrencyMappingDto toCurrencyMappingDto(HistoricalSecurityPriceCurrencyMapping currencyMapping);

  protected Map<String, HistoricalSecurityPriceCurrencyMapping> fromCurrencyMappingDto(List<CurrencyMappingDto> list) {
    Map<String, HistoricalSecurityPriceCurrencyMapping> result = new HashMap<>();
    if (list == null) {
      return result;
    }

    HistoricalSecurityPriceCurrencyMapping currencyMapping;
    for (CurrencyMappingDto currencyMappingDto : list) {
      currencyMapping = fromCurrencyMappingDto(currencyMappingDto);
      result.put(currencyMappingDto.getCurrencyKey(), currencyMapping);
    }
    return result;
  }

  protected List<CurrencyMappingDto> toCurrencyMappingDto(Map<String, HistoricalSecurityPriceCurrencyMapping> map) {
    List<CurrencyMappingDto> result = new LinkedList<>();
    if (map == null) {
      return result;
    }

    CurrencyMappingDto currencyMapping;
    for (Map.Entry<String, HistoricalSecurityPriceCurrencyMapping> entry : map.entrySet()) {
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

  @Named("fromHistoricalSecurityPriceUrlPatternDto")
  protected Map<Integer, String> fromHistoricalSecurityPriceUrlPatternDto(List<HistoricalSecurityPriceUrlPatternsDto> dtos) {
    Map<Integer, String> result = new HashMap<>();
    if (dtos == null) {
      return result;
    }

    for (HistoricalSecurityPriceUrlPatternsDto dto : dtos) {
      result.put(dto.getTimespanInDays(), dto.getUrlPattern());
    }
    return result;
  }

  @Named("toHistoricalSecurityPriceUrlPatternsDto")
  protected List<HistoricalSecurityPriceUrlPatternsDto> toHistoricalSecurityPriceUrlPatternsDto(
      Map<Integer, String> map) {
    List<HistoricalSecurityPriceUrlPatternsDto> result = new LinkedList<>();
    if (map == null) {
      return result;
    }

    for (Map.Entry<Integer, String> entry : map.entrySet()) {
      result.add(new HistoricalSecurityPriceUrlPatternsDto(entry.getKey(), entry.getValue()));
    }
    return result;
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
