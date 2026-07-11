package de.as.fynancials.price.security.historical.datasource;

import de.as.fynancials.price.security.historical.api.controller.HistoricalSecurityPriceDataSourceApiDelegate;
import de.as.fynancials.price.security.historical.api.model.HistoricalSecurityPriceDataSourceCreateDto;
import de.as.fynancials.price.security.historical.api.model.HistoricalSecurityPriceDataSourceReadDto;
import de.as.fynancials.price.security.historical.api.model.HistoricalSecurityPriceDataSourceUpdateDto;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
class HistoricalSecurityPriceDataSourceController implements HistoricalSecurityPriceDataSourceApiDelegate {

  private static final String URI_PATTERN = "/historicalprices/data-sources/%d";

  private final HistoricalSecurityPriceDataSourceService service;
  private final HistoricalSecurityPriceDataSourceMapper mapper;

  @Override
  public ResponseEntity<HistoricalSecurityPriceDataSourceReadDto> createHistoricalSecurityPriceDataSource(
      HistoricalSecurityPriceDataSourceCreateDto historicalSecurityPriceDataSourceCreateDto) {
    HistoricalSecurityPriceDataSource dataSource = mapper.fromCreateDto(historicalSecurityPriceDataSourceCreateDto);
    dataSource = service.createDataSource(dataSource);
    HistoricalSecurityPriceDataSourceReadDto responseBody = mapper.toDto(dataSource);
    URI locationHeader = URI.create(String.format(URI_PATTERN, dataSource.getId()));
    return ResponseEntity.created(locationHeader).body(responseBody);
  }

  @Override
  public ResponseEntity<Void> deleteHistoricalSecurityPriceDataSource(Long id) {
    service.deleteDataSource(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<List<HistoricalSecurityPriceDataSourceReadDto>> getHistoricalSecurityPriceDataSources() {
    List<HistoricalSecurityPriceDataSourceReadDto> responseBody = service.getDataSources().stream()
        .map(mapper::toDto)
        .toList();
    return ResponseEntity.ok(responseBody);
  }

  @Override
  public ResponseEntity<HistoricalSecurityPriceDataSourceReadDto> updateHistoricalSecurityPriceDataSource(
      Long id, HistoricalSecurityPriceDataSourceUpdateDto historicalSecurityPriceDataSourceUpdateDto) {
    HistoricalSecurityPriceDataSource dataSource = mapper.fromUpdateDto(historicalSecurityPriceDataSourceUpdateDto);
    dataSource.setId(id);
    dataSource = service.updateDataSource(dataSource);
    HistoricalSecurityPriceDataSourceReadDto responseBody = mapper.toDto(dataSource);
    return ResponseEntity.ok(responseBody);
  }
}
