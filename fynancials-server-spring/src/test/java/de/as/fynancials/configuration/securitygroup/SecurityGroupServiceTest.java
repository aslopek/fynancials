package de.as.fynancials.configuration.securitygroup;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.as.fynancials.common.error.ConflictException;
import de.as.fynancials.security.SecurityService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

class SecurityGroupServiceTest {

  private SecurityGroupRepository securityGroupRepository;
  private SecurityGroupServiceImpl subject;

  @BeforeEach
  void beforeEach() {
    securityGroupRepository = mock(SecurityGroupRepository.class);
    subject = new SecurityGroupServiceImpl(securityGroupRepository, mock(SecurityGroupMapper.class),
        mock(SecurityService.class));
  }

  @Test
  void deleteSecurityGroup_optimisticLockFails_conflict() {
    when(securityGroupRepository.findById(1L)).thenReturn(Optional.of(mock(SecurityGroupEntity.class)));
    doThrow(mock(ObjectOptimisticLockingFailureException.class)).when(securityGroupRepository).flush();

    assertThatThrownBy(() -> subject.deleteSecurityGroup(1L)).isInstanceOf(ConflictException.class);
  }
}
