package de.as.fynancials.notification.dividendannouncement.datasource;

import de.as.fynancials.notification.dividendannouncement.api.controller.DividendAnnouncementDataSourceApiDelegate;
import de.as.fynancials.notification.dividendannouncement.api.model.DividendAnnouncementDataSourceCreateDto;
import de.as.fynancials.notification.dividendannouncement.api.model.DividendAnnouncementDataSourceReadDto;
import de.as.fynancials.notification.dividendannouncement.api.model.DividendAnnouncementDataSourceUpdateDto;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
class DividendAnnouncementDataSourceController implements DividendAnnouncementDataSourceApiDelegate {

  private static final String DATA_SOURCE_URI_PATTERN = "/notifications/dividend-announcements/data-sources/%d";

  private final DividendAnnouncementDataSourceService dividendAnnouncementDataSourceService;
  private final DividendAnnouncementDataSourceMapper dividendAnnouncementDataSourceMapper;

  @Override
  public ResponseEntity<DividendAnnouncementDataSourceReadDto> createDividendAnnouncementDataSource(
      DividendAnnouncementDataSourceCreateDto dividendAnnouncementDataSourceCreateDto) {
    DividendAnnouncementDataSource dataSource =
        dividendAnnouncementDataSourceMapper.fromCreateDto(dividendAnnouncementDataSourceCreateDto);
    dataSource = dividendAnnouncementDataSourceService.createDataSource(dataSource);
    DividendAnnouncementDataSourceReadDto responseBody = dividendAnnouncementDataSourceMapper.toDto(dataSource);
    URI locationHeader = URI.create(String.format(DATA_SOURCE_URI_PATTERN, dataSource.getId()));
    return ResponseEntity.created(locationHeader).body(responseBody);
  }

  @Override
  public ResponseEntity<DividendAnnouncementDataSourceReadDto> updateDividendAnnouncementDataSource(Long id,
                                                                                                    DividendAnnouncementDataSourceUpdateDto dividendAnnouncementDataSourceUpdateDto) {
    DividendAnnouncementDataSource dataSource =
        dividendAnnouncementDataSourceMapper.fromUpdateDto(dividendAnnouncementDataSourceUpdateDto);
    dataSource.setId(id);
    dataSource = dividendAnnouncementDataSourceService.updateDataSource(dataSource);
    DividendAnnouncementDataSourceReadDto responseBody = dividendAnnouncementDataSourceMapper.toDto(dataSource);
    return ResponseEntity.ok(responseBody);
  }

  @Override
  public ResponseEntity<Void> deleteDividendAnnouncementDataSource(Long id) {
    dividendAnnouncementDataSourceService.deleteDataSource(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<List<DividendAnnouncementDataSourceReadDto>> getDividendAnnouncementDataSources() {
    List<DividendAnnouncementDataSourceReadDto> responseBody =
        dividendAnnouncementDataSourceService.getDataSources().stream().map(dividendAnnouncementDataSourceMapper::toDto)
            .toList();
    return ResponseEntity.ok(responseBody);
  }
}
