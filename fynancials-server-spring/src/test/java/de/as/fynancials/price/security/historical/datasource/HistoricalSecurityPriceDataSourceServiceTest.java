package de.as.fynancials.price.security.historical.datasource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.as.fynancials.common.error.ConflictException;
import de.as.fynancials.common.util.ValueFormatService;
import de.as.fynancials.configuration.ServerConfigurationService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

class HistoricalSecurityPriceDataSourceServiceTest {

  private HistoricalSecurityPriceDataSourceRepository repository;
  private HistoricalSecurityPriceDataSourceServiceImpl subject;

  @BeforeEach
  void beforeEach() {
    repository = mock(HistoricalSecurityPriceDataSourceRepository.class);
    subject = new HistoricalSecurityPriceDataSourceServiceImpl(repository,
        mock(HistoricalSecurityPriceDataSourceMapper.class), mock(ServerConfigurationService.class),
        mock(ValueFormatService.class));
  }

  @Test
  void deleteDataSource_optimisticLockFails_conflict() {
    when(repository.findById(101L)).thenReturn(Optional.of(mock(HistoricalSecurityPriceDataSourceEntity.class)));
    doThrow(mock(ObjectOptimisticLockingFailureException.class)).when(repository).flush();

    assertThatThrownBy(() -> subject.deleteDataSource(101L)).isInstanceOf(ConflictException.class);
  }
}
