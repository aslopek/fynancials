package de.as.traquity.depot;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.as.traquity.common.error.ConflictException;
import de.as.traquity.common.image.ImageService;
import de.as.traquity.configuration.ServerConfigurationService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

class DepotServiceTest {

  private DepotRepository depotRepository;
  private DepotServiceImpl subject;

  @BeforeEach
  void beforeEach() {
    depotRepository = mock(DepotRepository.class);
    subject = new DepotServiceImpl(mock(ImageService.class), mock(DepotLogoRepository.class), depotRepository,
        mock(DepotMapper.class), mock(ServerConfigurationService.class));
  }

  @Test
  void deleteDepot_optimisticLockFails_conflict() {
    when(depotRepository.findById(1L)).thenReturn(Optional.of(mock(DepotEntity.class)));
    doThrow(mock(ObjectOptimisticLockingFailureException.class)).when(depotRepository).flush();

    assertThatThrownBy(() -> subject.deleteDepot(1L)).isInstanceOf(ConflictException.class);
  }
}
