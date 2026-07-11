package de.as.fynancials.notification.dividendannouncement.config;

import de.as.fynancials.notification.dividendannouncement.api.controller.DividendAnnouncementConfigApiDelegate;
import de.as.fynancials.notification.dividendannouncement.api.model.DividendAnnouncementConfigCreateDto;
import de.as.fynancials.notification.dividendannouncement.api.model.DividendAnnouncementConfigReadDto;
import de.as.fynancials.notification.dividendannouncement.api.model.DividendAnnouncementConfigUpdateDto;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
class DividendAnnouncementConfigController implements DividendAnnouncementConfigApiDelegate {

  private static final String CONFIG_URI_PATTERN = "/notifications/dividend-announcements/configs/%d";

  private final DividendAnnouncementConfigService dividendAnnouncementConfigService;
  private final DividendAnnouncementConfigMapper dividendAnnouncementConfigMapper;

  @Override
  public ResponseEntity<DividendAnnouncementConfigReadDto> createDividendAnnouncementConfig(Long securityId,
                                                                                            DividendAnnouncementConfigCreateDto dividendAnnouncementConfigCreateDto) {
    DividendAnnouncementConfig newConfig =
        dividendAnnouncementConfigMapper.fromCreateDto(dividendAnnouncementConfigCreateDto);
    newConfig.setSecurityId(securityId);
    newConfig = dividendAnnouncementConfigService.createDividendAnnouncementConfig(newConfig);
    DividendAnnouncementConfigReadDto responseBody = dividendAnnouncementConfigMapper.toDto(newConfig);
    URI locationHeader = URI.create(String.format(CONFIG_URI_PATTERN, newConfig.getSecurityId()));
    return ResponseEntity.created(locationHeader).body(responseBody);
  }

  @Override
  public ResponseEntity<Void> deleteDividendAnnouncementConfig(Long securityId) {
    dividendAnnouncementConfigService.deleteDividendAnnouncementConfig(securityId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<DividendAnnouncementConfigReadDto> getDividendAnnouncementConfig(Long securityId) {
    DividendAnnouncementConfig config = dividendAnnouncementConfigService.getDividendAnnouncementConfig(securityId);
    DividendAnnouncementConfigReadDto responseBody = dividendAnnouncementConfigMapper.toDto(config);
    return ResponseEntity.ok(responseBody);
  }

  @Override
  public ResponseEntity<List<DividendAnnouncementConfigReadDto>> getDividendAnnouncementConfigs() {
    List<DividendAnnouncementConfigReadDto> responseBody =
        dividendAnnouncementConfigService.getDividendAnnouncementConfigs().stream()
            .map(dividendAnnouncementConfigMapper::toDto).toList();
    return ResponseEntity.ok(responseBody);
  }

  @Override
  public ResponseEntity<DividendAnnouncementConfigReadDto> updateDividendAnnouncementConfig(Long securityId,
                                                                                            DividendAnnouncementConfigUpdateDto dividendAnnouncementConfigUpdateDto) {
    DividendAnnouncementConfig config =
        dividendAnnouncementConfigMapper.fromUpdateDto(dividendAnnouncementConfigUpdateDto);
    config.setSecurityId(securityId);
    config = dividendAnnouncementConfigService.updateDividendAnnouncementConfig(config);
    DividendAnnouncementConfigReadDto responseBody = dividendAnnouncementConfigMapper.toDto(config);
    return ResponseEntity.ok(responseBody);
  }
}
