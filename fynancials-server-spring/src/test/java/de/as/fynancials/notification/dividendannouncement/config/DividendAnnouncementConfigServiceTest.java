package de.as.fynancials.notification.dividendannouncement.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.as.fynancials.common.error.ConflictException;
import de.as.fynancials.notification.dividendannouncement.datasource.DividendAnnouncementDataSourceService;
import de.as.fynancials.security.SecurityService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

class DividendAnnouncementConfigServiceTest {

  private DividendAnnouncementConfigRepository dividendAnnouncementConfigRepository;
  private DividendAnnouncementConfigServiceImpl subject;

  @BeforeEach
  void beforeEach() {
    dividendAnnouncementConfigRepository = mock(DividendAnnouncementConfigRepository.class);
    subject = new DividendAnnouncementConfigServiceImpl(dividendAnnouncementConfigRepository,
        mock(DividendAnnouncementConfigMapper.class), mock(SecurityService.class),
        mock(DividendAnnouncementDataSourceService.class));
  }

  @Test
  void deleteDividendAnnouncementConfig_optimisticLockFails_conflict() {
    when(dividendAnnouncementConfigRepository.findBySecurityId(1L))
        .thenReturn(Optional.of(mock(DividendAnnouncementConfigEntity.class)));
    doThrow(mock(ObjectOptimisticLockingFailureException.class)).when(dividendAnnouncementConfigRepository).flush();

    assertThatThrownBy(() -> subject.deleteDividendAnnouncementConfig(1L)).isInstanceOf(ConflictException.class);
  }
}
