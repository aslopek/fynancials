package de.as.fynancials.notification.dividendannouncement;

import de.as.fynancials.common.error.BadRequestException;
import de.as.fynancials.notification.dividendannouncement.api.controller.DividendAnnouncementApiDelegate;
import de.as.fynancials.notification.dividendannouncement.api.model.DividendAnnouncementReadDto;
import de.as.fynancials.notification.dividendannouncement.api.model.DividendAnnouncementUpdateDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
class DividendAnnouncementController implements DividendAnnouncementApiDelegate {

  private final DividendAnnouncementService dividendAnnouncementService;
  private final DividendAnnouncementMapper dividendAnnouncementMapper;

  @Override
  public ResponseEntity<List<DividendAnnouncementReadDto>> getDividendAnnouncements(Boolean isNew) {
    List<DividendAnnouncementReadDto> responseBody =
        dividendAnnouncementService.getDividendAnnouncements(isNew).stream().map(dividendAnnouncementMapper::toDto)
            .toList();
    return ResponseEntity.ok(responseBody);
  }

  @Override
  public ResponseEntity<Void> updateDividendAnnouncement(Long id,
                                                         DividendAnnouncementUpdateDto dividendAnnouncementUpdateDto) {
    if (dividendAnnouncementUpdateDto.getIsNew()) {
      throw new BadRequestException();
    }
    dividendAnnouncementService.markAsRead(id);
    return ResponseEntity.noContent().build();
  }
}
