package de.as.fynancials.depot.transaction;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.as.fynancials.common.error.ConflictException;
import java.math.MathContext;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

class TransactionServiceTest {

  private TransactionRepository transactionRepository;
  private TransactionServiceImpl subject;

  @BeforeEach
  void beforeEach() {
    transactionRepository = mock(TransactionRepository.class);
    subject = new TransactionServiceImpl(mock(TransactionMapper.class), transactionRepository, MathContext.DECIMAL64);
  }

  @Test
  void deleteTransaction_optimisticLockFails_conflict() {
    when(transactionRepository.findByIdAndDepotId(2L, 1L)).thenReturn(Optional.of(mock(TransactionEntity.class)));
    doThrow(mock(ObjectOptimisticLockingFailureException.class)).when(transactionRepository).flush();

    assertThatThrownBy(() -> subject.deleteTransaction(1L, 2L)).isInstanceOf(ConflictException.class);
  }
}
